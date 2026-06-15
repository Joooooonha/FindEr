import { useState, useEffect, useMemo, useRef } from 'react'
import KakaoMap from '../components/KakaoMap'
import HospitalPanel from '../components/HospitalPanel'
import { classifyHospital, matchedGroupIds, groupLabels } from '../components/treatmentGroups'
import { fetchHospitalDetail, fetchHospitals } from '../api/hospital'

const DEFAULT_LOCATION = { lat: 37.5665, lng: 126.9780 } // 서울 시청
const UPDATE_WINDOW_HOURS = {
  all: null,
  '1': 1,
  '3': 3,
  '6': 6,
  '12': 12,
}
const MAP_RESEARCH_THRESHOLD_METERS = 80

function getDistanceMeters(a, b) {
  if (!a || !b) return 0
  const toRad = value => (value * Math.PI) / 180
  const earthRadius = 6371000
  const lat1 = toRad(a.lat)
  const lat2 = toRad(b.lat)
  const dLat = toRad(b.lat - a.lat)
  const dLng = toRad(b.lng - a.lng)
  const h = Math.sin(dLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2
  return 2 * earthRadius * Math.asin(Math.sqrt(h))
}

function getUpdatedTime(hospital) {
  const time = new Date(hospital.updatedAt).getTime()
  return Number.isNaN(time) ? 0 : time
}

function hasAvailableBeds(hospital) {
  return Number.isInteger(hospital.availableBeds) && hospital.availableBeds > 0
}

function isUpdatedWithin(hospital, hours) {
  if (!hours) return true
  const updatedAt = getUpdatedTime(hospital)
  if (!updatedAt) return false
  return Date.now() - updatedAt <= hours * 60 * 60 * 1000
}

function sortHospitals(hospitals, sortBy) {
  const sorted = [...hospitals]
  sorted.sort((a, b) => {
    if (sortBy === 'beds') {
      const aBeds = Number.isInteger(a.availableBeds) ? a.availableBeds : -1
      const bBeds = Number.isInteger(b.availableBeds) ? b.availableBeds : -1
      if (bBeds !== aBeds) return bBeds - aBeds
    }

    if (sortBy === 'updated') {
      const diff = getUpdatedTime(b) - getUpdatedTime(a)
      if (diff !== 0) return diff
    }

    return Number(a.distance ?? Infinity) - Number(b.distance ?? Infinity)
  })
  return sorted
}

export default function MapPage() {
  const inflightDetailIdsRef = useRef(new Set())
  const [gpsLocation, setGpsLocation] = useState(null)
  const [customLocation, setCustomLocation] = useState(null)
  const [viewportCenter, setViewportCenter] = useState(null)
  const [hospitals, setHospitals] = useState([])
  const [radius, setRadius] = useState(5)
  const [selectedTreatments, setSelectedTreatments] = useState([])
  const [selectedHospital, setSelectedHospital] = useState(null)
  const [expandedHospitalIds, setExpandedHospitalIds] = useState([])
  const [hospitalDetails, setHospitalDetails] = useState({})
  const [detailLoadingById, setDetailLoadingById] = useState({})
  const [detailErrorById, setDetailErrorById] = useState({})
  const [loading, setLoading] = useState(false)
  const [sortBy, setSortBy] = useState('distance')
  const [onlyAvailableBeds, setOnlyAvailableBeds] = useState(false)
  const [updateWindow, setUpdateWindow] = useState('all')
  const [refreshKey, setRefreshKey] = useState(0)
  const [recenterKey, setRecenterKey] = useState(0)
  const [lastFetchedAt, setLastFetchedAt] = useState(null)

  const userLocation = customLocation ?? gpsLocation
  const mapMovedFromSearchLocation = useMemo(
    () => getDistanceMeters(userLocation, viewportCenter) > MAP_RESEARCH_THRESHOLD_METERS,
    [userLocation, viewportCenter]
  )

  useEffect(() => {
    if (userLocation) setViewportCenter(userLocation)
  }, [userLocation])

  // GPS 기반 내 위치 가져오기
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      pos => setGpsLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => setGpsLocation(DEFAULT_LOCATION),
      { timeout: 5000 }
    )
  }, [])

  // 위치/반경 변경 시 병원 조회. 새 목록에 없는 병원이 선택 상태로 남지 않도록 초기화.
  useEffect(() => {
    if (!userLocation) return
    setLoading(true)
    let cancelled = false

    fetchHospitals(userLocation.lat, userLocation.lng, radius)
      .then(data => {
        if (cancelled) return
        setHospitals(data.hospitals || [])
        setLastFetchedAt(new Date())
      })
      .catch(() => {
        if (cancelled) return
        setHospitals([])
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [userLocation, radius, refreshKey])

  useEffect(() => {
    if (!userLocation) return
    setSelectedHospital(null)
    setExpandedHospitalIds([])
    setHospitalDetails({})
    setDetailLoadingById({})
    setDetailErrorById({})
    inflightDetailIdsRef.current.clear()
  }, [userLocation, radius])

  // 병상/업데이트 필터(확정 조건)를 먼저 적용해 정렬한 기준 목록.
  const baseHospitals = useMemo(() => {
    const updateHours = UPDATE_WINDOW_HOURS[updateWindow]
    const filtered = hospitals.filter(hospital => {
      const bedMatched = !onlyAvailableBeds || hasAvailableBeds(hospital)
      const updateMatched = isUpdatedWithin(hospital, updateHours)
      return bedMatched && updateMatched
    })
    return sortHospitals(filtered, sortBy)
  }, [hospitals, onlyAvailableBeds, updateWindow, sortBy])

  // 증상 필터는 탈락 대신 3단 분류한다. 매칭 병원에는 근거 라벨을 붙인다.
  const classified = useMemo(() => {
    const matched = []
    const noData = []
    const unavailable = []
    for (const hospital of baseHospitals) {
      const kind = classifyHospital(hospital.availableTreatments, selectedTreatments)
      if (kind === 'matched') {
        const labels = groupLabels(matchedGroupIds(hospital.availableTreatments, selectedTreatments))
        matched.push({ ...hospital, matchedTreatments: labels })
      } else if (kind === 'no_data') {
        noData.push(hospital)
      } else {
        unavailable.push(hospital)
      }
    }
    return { matched, noData, unavailable }
  }, [baseHospitals, selectedTreatments])

  // 지도/선택 정리에는 "수용 불가(확정)"만 제외하고 매칭+정보미보고를 노출한다.
  const visibleHospitals = useMemo(
    () => [...classified.matched, ...classified.noData],
    [classified]
  )

  // 필터링 결과에 선택된 병원이 빠지면 선택을 해제한다.
  useEffect(() => {
    if (!selectedHospital) return
    if (!visibleHospitals.some(h => h.id === selectedHospital.id)) {
      setSelectedHospital(null)
    }
  }, [visibleHospitals, selectedHospital])

  const loadHospitalDetail = (hospitalId) => {
    const id = String(hospitalId)
    if (hospitalDetails[id] || inflightDetailIdsRef.current.has(id)) return
    inflightDetailIdsRef.current.add(id)

    setDetailLoadingById(prev => ({ ...prev, [id]: true }))
    setDetailErrorById(prev => ({ ...prev, [id]: null }))

    fetchHospitalDetail(id)
      .then(data => {
        setHospitalDetails(prev => ({ ...prev, [id]: data }))
      })
      .catch(() => {
        setDetailErrorById(prev => ({ ...prev, [id]: '상세 정보를 불러오지 못했습니다.' }))
      })
      .finally(() => {
        inflightDetailIdsRef.current.delete(id)
        setDetailLoadingById(prev => ({ ...prev, [id]: false }))
      })
  }

  const handleHospitalSelect = (hospital) => {
    const id = String(hospital.id)
    const alreadyExpanded = expandedHospitalIds.includes(id)
    setSelectedHospital(hospital)
    setExpandedHospitalIds(prev => (
      alreadyExpanded ? prev.filter(expandedId => expandedId !== id) : [...prev, id]
    ))
    if (!alreadyExpanded) loadHospitalDetail(id)
  }

  const handleCloseHospitalDetail = (hospitalId) => {
    const id = String(hospitalId)
    setExpandedHospitalIds(prev => prev.filter(expandedId => expandedId !== id))
  }

  const handleSearchCurrentLocation = () => {
    const searchLocation = viewportCenter ?? userLocation
    if (searchLocation) {
      setCustomLocation({
        lat: searchLocation.lat,
        lng: searchLocation.lng,
        label: '지도 중심 위치',
      })
    }
    setSelectedHospital(null)
    setExpandedHospitalIds([])
    setHospitalDetails({})
    setDetailLoadingById({})
    setDetailErrorById({})
    inflightDetailIdsRef.current.clear()
    setRefreshKey(key => key + 1)
  }

  const handleReturnToSearchLocation = () => {
    if (!userLocation) return
    setViewportCenter(userLocation)
    setRecenterKey(key => key + 1)
  }

  if (!userLocation) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#6b7280', fontSize: '14px' }}>
        위치 정보를 불러오는 중...
      </div>
    )
  }

  return (
    <HospitalPanel
      hospitals={visibleHospitals}
      matchedHospitals={classified.matched}
      noDataHospitals={classified.noData}
      unavailableHospitals={classified.unavailable}
      hasTreatmentFilter={selectedTreatments.length > 0}
      totalCount={hospitals.length}
      loading={loading}
      radius={radius}
      onRadiusChange={setRadius}
      selectedHospital={selectedHospital}
      onSelect={handleHospitalSelect}
      onCloseDetail={handleCloseHospitalDetail}
      onLocate={setCustomLocation}
      isCustom={Boolean(customLocation)}
      customLabel={customLocation?.label}
      onResetToGps={() => setCustomLocation(null)}
      selectedTreatments={selectedTreatments}
      onTreatmentsChange={setSelectedTreatments}
      sortBy={sortBy}
      onSortChange={setSortBy}
      onlyAvailableBeds={onlyAvailableBeds}
      onOnlyAvailableBedsChange={setOnlyAvailableBeds}
      updateWindow={updateWindow}
      onUpdateWindowChange={setUpdateWindow}
      lastFetchedAt={lastFetchedAt}
      expandedHospitalIds={expandedHospitalIds}
      hospitalDetails={hospitalDetails}
      detailLoadingById={detailLoadingById}
      detailErrorById={detailErrorById}
    >
      <>
        <KakaoMap
          userLocation={userLocation}
          hospitals={visibleHospitals}
          selectedHospital={selectedHospital}
          onHospitalClick={handleHospitalSelect}
          onViewportCenterChange={setViewportCenter}
          recenterKey={recenterKey}
        />
        <div
          className={`map-center-search-marker${mapMovedFromSearchLocation ? ' is-active' : ''}`}
          aria-hidden="true"
        >
          <span className="map-center-search-dot" />
        </div>
        {mapMovedFromSearchLocation && (
          <div className="map-research-actions">
            <button
              type="button"
              className="map-return-button"
              onClick={handleReturnToSearchLocation}
            >
              현재 위치로
            </button>
            <button
              type="button"
              className="map-research-button"
              onClick={handleSearchCurrentLocation}
              disabled={loading}
            >
              {loading ? '검색 중' : '지도 중심에서 재검색'}
            </button>
          </div>
        )}
      </>
    </HospitalPanel>
  )
}
