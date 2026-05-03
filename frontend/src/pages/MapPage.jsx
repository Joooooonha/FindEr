import { useState, useEffect } from 'react'
import KakaoMap from '../components/KakaoMap'
import HospitalPanel from '../components/HospitalPanel'
import { fetchHospitals } from '../api/hospital'

const DEFAULT_LOCATION = { lat: 37.5665, lng: 126.9780 } // 서울 시청

export default function MapPage() {
  const [userLocation, setUserLocation] = useState(null)
  const [hospitals, setHospitals] = useState([])
  const [radius, setRadius] = useState(5)
  const [selectedHospital, setSelectedHospital] = useState(null)
  const [loading, setLoading] = useState(false)

  // 내 위치 가져오기
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      pos => setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => setUserLocation(DEFAULT_LOCATION),
      { timeout: 5000 }
    )
  }, [])

  // 위치/반경 변경 시 병원 조회
  useEffect(() => {
    if (!userLocation) return
    setLoading(true)
    fetchHospitals(userLocation.lat, userLocation.lng, radius)
      .then(data => setHospitals(data.hospitals || []))
      .catch(() => setHospitals([]))
      .finally(() => setLoading(false))
  }, [userLocation, radius])

  if (!userLocation) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', color: '#6b7280', fontSize: '14px' }}>
        위치 정보를 불러오는 중...
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      <HospitalPanel
        hospitals={hospitals}
        loading={loading}
        radius={radius}
        onRadiusChange={setRadius}
        selectedHospital={selectedHospital}
        onSelect={setSelectedHospital}
      />
      <KakaoMap
        userLocation={userLocation}
        hospitals={hospitals}
        selectedHospital={selectedHospital}
        onHospitalClick={setSelectedHospital}
      />
    </div>
  )
}
