const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const DUMMY_HOSPITALS = [
  { id: 'A1100', name: '서울대학교병원 응급실', address: '서울 종로구 대학로 101', phone: '02-2072-2974', distance: 1.2, status: 'GREEN',   availableBeds: 15, lat: 37.5796, lng: 126.9989 },
  { id: 'A1101', name: '세브란스병원 응급실',   address: '서울 서대문구 연세로 50', phone: '02-2228-1004', distance: 2.8, status: 'YELLOW',  availableBeds: 4,  lat: 37.5621, lng: 126.9409 },
  { id: 'A1102', name: '서울아산병원 응급실',   address: '서울 송파구 올림픽로 43길 88', phone: '02-3010-3333', distance: 4.1, status: 'RED',     availableBeds: 0,  lat: 37.5270, lng: 127.1086 },
  { id: 'A1103', name: '강남세브란스 응급실',   address: '서울 강남구 언주로 211', phone: '02-2019-3114', distance: 5.3, status: 'UNKNOWN', availableBeds: null, lat: 37.4882, lng: 127.0422 },
  { id: 'A1104', name: '한양대학교병원 응급실', address: '서울 성동구 왕십리로 222', phone: '02-2290-8114', distance: 3.6, status: 'GREEN',   availableBeds: 8,  lat: 37.5573, lng: 127.0211 },
]

export async function fetchHospitals(lat, lng, radius = 5) {
  try {
    const res = await fetch(`${BASE_URL}/api/v1/hospitals?lat=${lat}&lng=${lng}&radius=${radius}`)
    const data = await res.json()
    if (data.hospitals && data.hospitals.length > 0) return data
  } catch {}
  // API 미승인 기간 더미 데이터
  return { hospitals: DUMMY_HOSPITALS.filter(h => h.distance <= radius) }
}

export async function fetchHospitalDetail(id) {
  const res = await fetch(`${BASE_URL}/api/v1/hospitals/${id}`)
  return res.json()
}
