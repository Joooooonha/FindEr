import { useState } from 'react'

const MAX_SEARCH_RESULTS = 7

function toSearchResult(place, index) {
  return {
    id: place.id || `${place.x}-${place.y}-${index}`,
    name: place.place_name,
    address: place.road_address_name || place.address_name || '',
    category: place.category_group_name || place.category_name || '',
    lat: parseFloat(place.y),
    lng: parseFloat(place.x),
  }
}

/** 지도 기준 위치를 장소 검색 결과 중 하나로 변경한다. */
export default function LocationSearch({ onLocate, isCustom, onResetToGps }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState(null)

  const handleSearch = (e) => {
    e.preventDefault()
    const keyword = query.trim()
    if (!keyword) return
    if (!window.kakao?.maps?.services) {
      setError('지도 검색을 불러오지 못했습니다.')
      setResults([])
      return
    }
    setSearching(true)
    setError(null)

    const Status = window.kakao.maps.services.Status
    const places = new window.kakao.maps.services.Places()
    places.keywordSearch(keyword, (placesResults, status) => {
      setSearching(false)

      if (status === Status.ZERO_RESULT || !placesResults.length) {
        setResults([])
        setError('검색 결과가 없습니다.')
        return
      }

      if (status !== Status.OK) {
        setResults([])
        setError('검색 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.')
        return
      }

      setResults(placesResults.slice(0, MAX_SEARCH_RESULTS).map(toSearchResult))
    })
  }

  const handleSelect = (result) => {
    onLocate({ lat: result.lat, lng: result.lng, label: result.name })
    setQuery(result.name)
    setResults([])
    setError(null)
  }

  const handleQueryChange = (e) => {
    const value = e.target.value
    setQuery(value)
    if (!value.trim()) {
      setResults([])
      setError(null)
    }
  }

  return (
    <div style={{ padding: '12px 16px', borderBottom: '1px solid #e5e7eb' }}>
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '6px' }}>
        <input
          type="text"
          value={query}
          onChange={handleQueryChange}
          placeholder="지역·장소 검색 (예: 분당역)"
          style={{
            flex: 1,
            padding: '7px 10px',
            fontSize: '13px',
            border: '1px solid #d1d5db',
            borderRadius: '6px',
            outline: 'none',
          }}
        />
        <button
          type="submit"
          disabled={searching || !query.trim()}
          style={{
            padding: '7px 12px',
            fontSize: '13px',
            fontWeight: 500,
            color: '#fff',
            background: searching || !query.trim() ? '#9ca3af' : '#3b82f6',
            border: 'none',
            borderRadius: '6px',
            cursor: searching || !query.trim() ? 'not-allowed' : 'pointer',
            whiteSpace: 'nowrap',
          }}
        >
          {searching ? '...' : '검색'}
        </button>
      </form>

      {error && (
        <p style={{ fontSize: '11px', color: '#dc2626', marginTop: '6px' }}>{error}</p>
      )}

      {results.length > 0 && (
        <div style={{ marginTop: '8px' }}>
          <p style={{ fontSize: '11px', color: '#6b7280', marginBottom: '6px' }}>
            검색 결과 중 위치를 선택해 주세요.
          </p>
          <div
            style={{
              border: '1px solid #e5e7eb',
              borderRadius: '6px',
              overflow: 'hidden',
            }}
          >
            {results.map((result, index) => (
              <button
                key={result.id}
                type="button"
                onClick={() => handleSelect(result)}
                style={{
                  width: '100%',
                  display: 'block',
                  padding: '9px 10px',
                  textAlign: 'left',
                  background: '#fff',
                  border: 'none',
                  borderBottom: index === results.length - 1 ? 'none' : '1px solid #e5e7eb',
                  cursor: 'pointer',
                }}
              >
                <span style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#111827' }}>
                  {result.name}
                </span>
                {result.address && (
                  <span style={{ display: 'block', marginTop: '2px', fontSize: '11px', color: '#6b7280' }}>
                    {result.address}
                  </span>
                )}
                {result.category && (
                  <span style={{ display: 'block', marginTop: '2px', fontSize: '11px', color: '#9ca3af' }}>
                    {result.category}
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>
      )}

      {isCustom && (
        <button
          type="button"
          onClick={onResetToGps}
          style={{
            marginTop: '8px',
            padding: '4px 10px',
            fontSize: '11px',
            color: '#3b82f6',
            background: '#eff6ff',
            border: '1px solid #bfdbfe',
            borderRadius: '12px',
            cursor: 'pointer',
          }}
        >
          내 현재 위치로 돌아가기
        </button>
      )}
    </div>
  )
}
