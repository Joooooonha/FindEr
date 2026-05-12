import { useState, useEffect, useMemo } from 'react'
import KakaoMap from '../components/KakaoMap'
import HospitalPanel from '../components/HospitalPanel'
import { matchesAllGroups } from '../components/TreatmentFilter'
import { fetchHospitalDetail, fetchHospitals } from '../api/hospital'

const DEFAULT_LOCATION = { lat: 37.5665, lng: 126.9780 } // 서울 시청

export default function MapPage() {
  const [gpsLocation, setGpsLocation] = useState(null)
  const [customLocation, setCustomLocation] = useState(null)
  const [hospitals, setHospitals] = useState([])
  const [radius, setRadius] = useState(5)
  const [selectedTreatments, setSelectedTreatments] = useState([])
  const [selectedHospital, setSelectedHospital] = useState(null)
  const [selectedHospitalDetail, setSelectedHospitalDetail] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState(null)
  const [loading, setLoading] = useState(false)

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
    setSelectedHospital(null)
    setLoading(true)
    fetchHospitals(userLocation.lat, userLocation.lng, radius)
      .then(data => setHospitals(data.hospitals || []))
      .catch(() => setHospitals([]))
      .finally(() => setLoading(false))
  }, [userLocation, radius])

  // 선택된 증상 필터에 따라 병원 목록 필터링
  const visibleHospitals = useMemo(() => {
    if (selectedTreatments.length === 0) return hospitals
    return hospitals.filter(h => matchesAllGroups(h.availableTreatments, selectedTreatments))
  }, [hospitals, selectedTreatments])

  // 필터링 결과에 선택된 병원이 빠지면 선택을 해제한다.
  useEffect(() => {
    if (!selectedHospital) return
    if (!visibleHospitals.some(h => h.id === selectedHospital.id)) {
      setSelectedHospital(null)
    }
  }, [visibleHospitals, selectedHospital])

  useEffect(() => {
    if (!selectedHospital) {
      setSelectedHospitalDetail(null)
      setDetailError(null)
      setDetailLoading(false)
      return
    }

    let ignore = false
    setSelectedHospitalDetail(null)
    setDetailError(null)
    setDetailLoading(true)

    fetchHospitalDetail(selectedHospital.id)
      .then(data => {
        if (!ignore) setSelectedHospitalDetail(data)
      })
      .catch(() => {
        if (!ignore) setDetailError('상세 정보를 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!ignore) setDetailLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [selectedHospital])

  if (!userLocation) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#6b7280', fontSize: '14px' }}>
        위치 정보를 불러오는 중...
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <HospitalPanel
        hospitals={visibleHospitals}
        totalCount={hospitals.length}
        loading={loading}
        radius={radius}
        onRadiusChange={setRadius}
        selectedHospital={selectedHospital}
        selectedHospitalDetail={selectedHospitalDetail}
        detailLoading={detailLoading}
        detailError={detailError}
        onSelect={setSelectedHospital}
        onCloseDetail={() => setSelectedHospital(null)}
        onLocate={setCustomLocation}
        isCustom={Boolean(customLocation)}
        customLabel={customLocation?.label}
        onResetToGps={() => setCustomLocation(null)}
        selectedTreatments={selectedTreatments}
        onTreatmentsChange={setSelectedTreatments}
      />
      <KakaoMap
        userLocation={userLocation}
        hospitals={visibleHospitals}
        selectedHospital={selectedHospital}
        onHospitalClick={setSelectedHospital}
      />
    </div>
  )
}
