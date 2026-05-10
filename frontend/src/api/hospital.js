// 환경변수 trailing slash 가 들어와도 // 가 안 생기게 정규화한다.
const BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')

export async function fetchHospitals(lat, lng, radius = 5) {
  const res = await fetch(`${BASE_URL}/api/v1/hospitals?lat=${lat}&lng=${lng}&radius=${radius}`)
  if (!res.ok) throw new Error(`병원 조회 실패 (${res.status})`)
  return res.json()
}

export async function fetchHospitalDetail(id) {
  const res = await fetch(`${BASE_URL}/api/v1/hospitals/${encodeURIComponent(String(id))}`)
  if (!res.ok) throw new Error(`병원 상세 조회 실패 (${res.status})`)
  return res.json()
}
