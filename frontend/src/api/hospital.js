const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const DUMMY_HOSPITALS = [
  { id: 'A1100', name: '서울대학교병원 응급실',    address: '서울 종로구 대학로 101',              phone: '02-2072-2974', status: 'GREEN',   availableBeds: 15,   lat: 37.5796, lng: 126.9989 },
  { id: 'A1101', name: '세브란스병원 응급실',       address: '서울 서대문구 연세로 50',             phone: '02-2228-1004', status: 'YELLOW',  availableBeds: 4,    lat: 37.5621, lng: 126.9409 },
  { id: 'A1102', name: '서울아산병원 응급실',       address: '서울 송파구 올림픽로 43길 88',        phone: '02-3010-3333', status: 'RED',     availableBeds: 0,    lat: 37.5270, lng: 127.1086 },
  { id: 'A1103', name: '강남세브란스병원 응급실',   address: '서울 강남구 언주로 211',              phone: '02-2019-3114', status: 'UNKNOWN', availableBeds: null, lat: 37.4882, lng: 127.0422 },
  { id: 'A1104', name: '한양대학교병원 응급실',     address: '서울 성동구 왕십리로 222',            phone: '02-2290-8114', status: 'GREEN',   availableBeds: 8,    lat: 37.5573, lng: 127.0211 },
  { id: 'A1105', name: '분당서울대학교병원 응급실', address: '경기 성남시 분당구 구미로 173번길 82', phone: '031-787-7575', status: 'GREEN',   availableBeds: 12,   lat: 37.3598, lng: 127.1049 },
  { id: 'A1106', name: '분당차병원 응급실',         address: '경기 성남시 분당구 야탑로 59',        phone: '031-780-5000', status: 'YELLOW',  availableBeds: 3,    lat: 37.3544, lng: 127.0917 },
  { id: 'A1107', name: '아주대학교병원 응급실',     address: '경기 수원시 영통구 월드컵로 164',     phone: '031-219-5119', status: 'GREEN',   availableBeds: 9,    lat: 37.2785, lng: 127.0437 },
  { id: 'A1108', name: '성남시의료원 응급실',       address: '경기 성남시 수정구 수정로 171',       phone: '031-738-7000', status: 'UNKNOWN', availableBeds: null, lat: 37.4337, lng: 127.1380 },
]

function haversineKm(lat1, lng1, lat2, lng2) {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2
    + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export async function fetchHospitals(lat, lng, radius = 5) {
  try {
    const res = await fetch(`${BASE_URL}/api/v1/hospitals?lat=${lat}&lng=${lng}&radius=${radius}`)
    const data = await res.json()
    if (data.hospitals && data.hospitals.length > 0) return data
  } catch {}
  // E-Gen API 미승인 기간 더미 데이터 (실제 위치 기반 거리 계산)
  const hospitals = DUMMY_HOSPITALS
    .map(h => ({ ...h, distance: Math.round(haversineKm(lat, lng, h.lat, h.lng) * 10) / 10 }))
    .filter(h => h.distance <= radius)
    .sort((a, b) => a.distance - b.distance)
  return { hospitals }
}

export async function fetchHospitalDetail(id) {
  const res = await fetch(`${BASE_URL}/api/v1/hospitals/${id}`)
  return res.json()
}
