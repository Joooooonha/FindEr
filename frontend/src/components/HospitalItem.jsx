import StatusBadge from './StatusBadge'

/** 카카오맵 길찾기 URL 생성. 한글 이름 안전을 위해 encodeURIComponent 처리. */
function buildKakaoDirectionsUrl(hospital) {
  const name = encodeURIComponent(hospital.name)
  return `https://map.kakao.com/link/to/${name},${hospital.lat},${hospital.lng}`
}

export default function HospitalItem({ hospital, isSelected, onClick }) {
  const hasCoords = Number.isFinite(hospital.lat) && Number.isFinite(hospital.lng)

  return (
    <div
      onClick={onClick}
      style={{
        padding: '14px 16px',
        borderBottom: '1px solid #f3f4f6',
        cursor: 'pointer',
        background: isSelected ? '#eff6ff' : '#fff',
        borderLeft: `3px solid ${isSelected ? '#3b82f6' : 'transparent'}`,
        transition: 'background 0.15s',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '8px' }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: '14px', fontWeight: 600, color: '#111827', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {hospital.name}
          </p>
          <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '2px' }}>
            {hospital.distance}km
          </p>
        </div>
        <StatusBadge status={hospital.status} />
      </div>

      <div style={{ display: 'flex', gap: '12px', marginTop: '8px', alignItems: 'center' }}>
        {hospital.phone && (
          <a
            href={`tel:${hospital.phone}`}
            onClick={e => e.stopPropagation()}
            style={{ fontSize: '12px', color: '#3b82f6', textDecoration: 'none' }}
          >
            📞 {hospital.phone}
          </a>
        )}
        {hasCoords && (
          <a
            href={buildKakaoDirectionsUrl(hospital)}
            target="_blank"
            rel="noopener noreferrer"
            onClick={e => e.stopPropagation()}
            style={{ fontSize: '12px', color: '#3b82f6', textDecoration: 'none' }}
          >
            🧭 길찾기
          </a>
        )}
      </div>
    </div>
  )
}
