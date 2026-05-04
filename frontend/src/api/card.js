const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export async function createCard(payload) {
  const res = await fetch(`${BASE_URL}/api/v1/cards`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw await toError(res)
  return res.json()
}

export async function fetchCard(token) {
  const res = await fetch(`${BASE_URL}/api/v1/cards/${token}`)
  if (!res.ok) throw await toError(res)
  return res.json()
}

export async function updateCard(token, pin, payload) {
  const res = await fetch(`${BASE_URL}/api/v1/cards/${token}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-Card-Pin': pin },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw await toError(res)
  return res.json()
}

export async function deleteCard(token, pin) {
  const res = await fetch(`${BASE_URL}/api/v1/cards/${token}`, {
    method: 'DELETE',
    headers: { 'X-Card-Pin': pin },
  })
  if (!res.ok) throw await toError(res)
}

async function toError(res) {
  try {
    const data = await res.json()
    return new Error(data.message || `요청 실패 (${res.status})`)
  } catch {
    return new Error(`요청 실패 (${res.status})`)
  }
}
