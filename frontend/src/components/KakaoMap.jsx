import { useEffect, useRef } from 'react'

const STATUS_COLORS = {
  GREEN:   '#22c55e',
  YELLOW:  '#eab308',
  RED:     '#ef4444',
  UNKNOWN: '#94a3b8',
}

function getBedLabel(hospital) {
  if (!Number.isInteger(hospital.availableBeds) || hospital.availableBeds < 0) return '?'
  return String(hospital.availableBeds)
}

export default function KakaoMap({
  userLocation,
  hospitals,
  selectedHospital,
  onHospitalClick,
  onViewportCenterChange,
}) {
  const containerRef = useRef(null)
  const mapRef = useRef(null)
  const overlaysRef = useRef([])
  const userMarkerRef = useRef(null)
  const onViewportCenterChangeRef = useRef(onViewportCenterChange)

  useEffect(() => {
    onViewportCenterChangeRef.current = onViewportCenterChange
  }, [onViewportCenterChange])

  // 최초 지도 생성 (마운트 시 1회)
  useEffect(() => {
    if (!containerRef.current || !window.kakao) return
    const { kakao } = window
    const center = new kakao.maps.LatLng(userLocation.lat, userLocation.lng)

    mapRef.current = new kakao.maps.Map(containerRef.current, { center, level: 6 })
    userMarkerRef.current = new kakao.maps.Marker({ position: center, map: mapRef.current })

    const handleMapMoved = () => {
      const currentCenter = mapRef.current.getCenter()
      onViewportCenterChangeRef.current?.({
        lat: currentCenter.getLat(),
        lng: currentCenter.getLng(),
      })
    }

    kakao.maps.event.addListener(mapRef.current, 'dragend', handleMapMoved)
    kakao.maps.event.addListener(mapRef.current, 'zoom_changed', handleMapMoved)

    return () => {
      kakao.maps.event.removeListener(mapRef.current, 'dragend', handleMapMoved)
      kakao.maps.event.removeListener(mapRef.current, 'zoom_changed', handleMapMoved)
      overlaysRef.current.forEach(o => o.setMap(null))
      overlaysRef.current = []
      userMarkerRef.current?.setMap(null)
      userMarkerRef.current = null
      mapRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // 사용자 위치 변경 시 지도 중심 이동 + 사용자 마커 위치 업데이트
  useEffect(() => {
    if (!mapRef.current || !window.kakao) return
    const center = new window.kakao.maps.LatLng(userLocation.lat, userLocation.lng)
    mapRef.current.setCenter(center)
    userMarkerRef.current?.setPosition(center)
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
      const selected = selectedHospital?.id === hospital.id

      const marker = document.createElement('button')
      marker.type = 'button'
      marker.title = `${hospital.name} · 응급실 병상 ${getBedLabel(hospital)}개`
      marker.style.cssText = `
        min-width: ${selected ? '34px' : '28px'};
        height: ${selected ? '34px' : '28px'};
        padding: 0 7px;
        background: ${color};
        border-radius: 999px;
        border: ${selected ? '3px solid #111827' : '2px solid white'};
        box-shadow: 0 3px 8px rgba(15,23,42,0.28);
        cursor: pointer;
        color: white;
        font-size: ${selected ? '13px' : '12px'};
        font-weight: 800;
        line-height: 1;
        text-align: center;
      `
      marker.textContent = getBedLabel(hospital)
      marker.addEventListener('click', () => onHospitalClick(hospital))

      const overlay = new kakao.maps.CustomOverlay({ position, content: marker, map: mapRef.current })
      overlaysRef.current.push(overlay)
    })
  }, [hospitals, selectedHospital, onHospitalClick])

  // 선택된 병원으로 지도 이동
  useEffect(() => {
    if (!mapRef.current || !selectedHospital || !window.kakao) return
    mapRef.current.panTo(new window.kakao.maps.LatLng(selectedHospital.lat, selectedHospital.lng))
  }, [selectedHospital])

  return <div ref={containerRef} style={{ flex: 1, height: '100%' }} />
}
