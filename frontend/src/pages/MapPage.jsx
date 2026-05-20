import { useState, useEffect, useMemo } from 'react'
import KakaoMap from '../components/KakaoMap'
import HospitalPanel from '../components/HospitalPanel'
import { matchesAllGroups } from '../components/TreatmentFilter'
import { fetchHospitalDetail, fetchHospitals } from '../api/hospital'

const DEFAULT_LOCATION = { lat: 37.5665, lng: 126.9780 } // 서울 시청
const UPDATE_WINDOW_HOURS = {
  all: null,
  '1': 1,
  '3': 3,
  '6': 6,
  '12': 12,
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
  const [gpsLocation, setGpsLocation] = useState(null)
  const [customLocation, setCustomLocation] = useState(null)
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
  const [lastFetchedAt, setLastFetchedAt] = useState(null)

  const userLocation = customLocation ?? gpsLocation

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
  }, [userLocation, radius])

  // 선택된 증상/목록 필터와 정렬 조건에 따라 병원 목록을 계산한다.
  const visibleHospitals = useMemo(() => {
    const updateHours = UPDATE_WINDOW_HOURS[updateWindow]
    const filtered = hospitals.filter(hospital => {
      const treatmentMatched = selectedTreatments.length === 0
        || matchesAllGroups(hospital.availableTreatments, selectedTreatments)
      const bedMatched = !onlyAvailableBeds || hasAvailableBeds(hospital)
      const updateMatched = isUpdatedWithin(hospital, updateHours)
      return treatmentMatched && bedMatched && updateMatched
    })
    return sortHospitals(filtered, sortBy)
  }, [hospitals, selectedTreatments, onlyAvailableBeds, updateWindow, sortBy])

  // 필터링 결과에 선택된 병원이 빠지면 선택을 해제한다.
  useEffect(() => {
    if (!selectedHospital) return
    if (!visibleHospitals.some(h => h.id === selectedHospital.id)) {
      setSelectedHospital(null)
    }
  }, [visibleHospitals, selectedHospital])

  const loadHospitalDetail = (hospitalId) => {
    const id = String(hospitalId)
    if (hospitalDetails[id] || detailLoadingById[id]) return

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
    setCustomLocation(null)
    setRefreshKey(key => key + 1)
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
      onSearchCurrentLocation={handleSearchCurrentLocation}
      lastFetchedAt={lastFetchedAt}
      expandedHospitalIds={expandedHospitalIds}
      hospitalDetails={hospitalDetails}
      detailLoadingById={detailLoadingById}
      detailErrorById={detailErrorById}
    >
      <KakaoMap
        userLocation={userLocation}
        hospitals={visibleHospitals}
        selectedHospital={selectedHospital}
        onHospitalClick={handleHospitalSelect}
      />
    </HospitalPanel>
  )
}
