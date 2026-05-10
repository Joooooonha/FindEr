const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
const TIMEOUT_MS = 10000

async function request(path, options = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS)
  try {
    const res = await fetch(`${BASE_URL}${path}`, { ...options, signal: controller.signal })
    if (!res.ok) throw await toError(res)
    return res
  } catch (err) {
    if (err.name === 'AbortError') throw new Error('요청 시간이 초과되었습니다')
    throw err
  } finally {
    clearTimeout(timer)
  }
}

export async function createCard(payload) {
  const res = await request('/api/v1/cards', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return res.json()
}

export async function fetchCard(token) {
  const res = await request(`/api/v1/cards/${token}`)
  return res.json()
}

export async function updateCard(token, pin, payload) {
  const res = await request(`/api/v1/cards/${token}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-Card-Pin': pin },
    body: JSON.stringify(payload),
  })
  return res.json()
}

export async function deleteCard(token, pin) {
  await request(`/api/v1/cards/${token}`, {
    method: 'DELETE',
    headers: { 'X-Card-Pin': pin },
  })
}

async function toError(res) {
  try {
    const data = await res.json()
    return new Error(data.message || `요청 실패 (${res.status})`)
  } catch {
    return new Error(`요청 실패 (${res.status})`)
  }
}
