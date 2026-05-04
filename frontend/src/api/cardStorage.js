const TOKEN_KEY = 'finder.cardToken'

export function saveMyCardToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getMyCardToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function clearMyCardToken() {
  localStorage.removeItem(TOKEN_KEY)
}
