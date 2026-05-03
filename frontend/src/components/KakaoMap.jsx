import { useEffect, useRef } from 'react'

const STATUS_COLORS = {
  GREEN:   '#22c55e',
  YELLOW:  '#eab308',
  RED:     '#ef4444',
  UNKNOWN: '#94a3b8',
}

export default function KakaoMap({ userLocation, hospitals, selectedHospital, onHospitalClick }) {
  const containerRef = useRef(null)
  const mapRef = useRef(null)
  const overlaysRef = useRef([])
  const userMarkerRef = useRef(null)

  // 최초 지도 생성
  useEffect(() => {
    if (!containerRef.current || !window.kakao) return
    const { kakao } = window
    const center = new kakao.maps.LatLng(userLocation.lat, userLocation.lng)

    mapRef.current = new kakao.maps.Map(containerRef.current, { center, level: 6 })

    // 내 위치 마커
    userMarkerRef.current = new kakao.maps.Marker({ position: center, map: mapRef.current })
  }, [userLocation])

  // 병원 마커 업데이트
  useEffect(() => {
    if (!mapRef.current || !window.kakao) return
    const { kakao } = window

    // 기존 오버레이 제거
    overlaysRef.current.forEach(o => o.setMap(null))
    overlaysRef.current = []

    hospitals.forEach(hospital => {
      const position = new kakao.maps.LatLng(hospital.lat, hospital.lng)
      const color = STATUS_COLORS[hospital.status] || STATUS_COLORS.UNKNOWN

      const dot = document.createElement('div')
      dot.style.cssText = `
        width: 14px; height: 14px;
        background: ${color};
        border-radius: 50%;
        border: 2px solid white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        cursor: pointer;
      `
      dot.addEventListener('click', () => onHospitalClick(hospital))

      const overlay = new kakao.maps.CustomOverlay({ position, content: dot, map: mapRef.current })
      overlaysRef.current.push(overlay)
    })
  }, [hospitals, onHospitalClick])

  // 선택된 병원으로 지도 이동
  useEffect(() => {
    if (!mapRef.current || !selectedHospital || !window.kakao) return
    mapRef.current.panTo(new window.kakao.maps.LatLng(selectedHospital.lat, selectedHospital.lng))
  }, [selectedHospital])

  return <div ref={containerRef} style={{ flex: 1, height: '100vh' }} />
}
