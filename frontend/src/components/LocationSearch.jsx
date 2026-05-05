import { useState } from 'react'

/** 카카오 키워드 검색으로 좌표를 얻어 부모로 전달한다. */
export default function LocationSearch({ onLocate, isCustom, onResetToGps }) {
  const [query, setQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState(null)

  const handleSearch = (e) => {
    e.preventDefault()
    const keyword = query.trim()
    if (!keyword) return
    if (!window.kakao?.maps?.services) {
      setError('지도 검색 서비스 로드 실패')
      return
    }
    setSearching(true)
    setError(null)

    const Status = window.kakao.maps.services.Status
    const places = new window.kakao.maps.services.Places()
    places.keywordSearch(keyword, (results, status) => {
      setSearching(false)
      if (status === Status.ZERO_RESULT || !results.length) {
        setError('검색 결과가 없습니다')
        return
      }
      if (status !== Status.OK) {
        setError('검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요')
        return
      }
      const top = results[0]
      onLocate({ lat: parseFloat(top.y), lng: parseFloat(top.x), label: top.place_name })
      setQuery('')
    })
  }

  return (
    <div style={{ padding: '12px 16px', borderBottom: '1px solid #e5e7eb' }}>
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '6px' }}>
        <input
          type="text"
          value={query}
          onChange={e => setQuery(e.target.value)}
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
          📍 내 위치로 돌아가기
        </button>
      )}
    </div>
  )
}
