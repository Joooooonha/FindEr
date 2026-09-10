import { BED_UPDATE_HELP } from '../constants/hospitalConstants'
import StatusBadge from './StatusBadge'
import { getMatchedGroups } from './TreatmentFilter'

/** 카카오맵 길찾기 URL 생성. 한글 이름 안전을 위해 encodeURIComponent 처리. */
function buildKakaoDirectionsUrl(hospital) {
  const name = encodeURIComponent(hospital.name)
  return `https://map.kakao.com/link/to/${name},${hospital.lat},${hospital.lng}`
}

/** 갱신 시각을 상대 시간 문자열로 변환한다. */
function toRelativeTime(isoString) {
  if (!isoString) return null
  const updated = new Date(isoString)
  if (Number.isNaN(updated.getTime())) return null
  const diffMs = Date.now() - updated.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}시간 전`
  return updated.toLocaleDateString('ko-KR')
}

export default function HospitalItem({
  hospital,
  isSelected,
  selectedTreatments = [],
  onClick,
}) {
  const hasCoords = Number.isFinite(hospital.lat) && Number.isFinite(hospital.lng)
  const showBeds = Number.isInteger(hospital.availableBeds) && hospital.availableBeds >= 0
  const relativeTime = toRelativeTime(hospital.updatedAt)
  // 선택된 증상 필터 중 이 병원이 실제로 처치 가능한 항목만 추려, 매칭 근거를 카드에 바로 노출한다.
  const matchedActiveGroups = selectedTreatments.length > 0
    ? getMatchedGroups(hospital.availableTreatments).filter(g => selectedTreatments.includes(g.id))
    : []

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

      {matchedActiveGroups.length > 0 && (
        <div style={{ display: 'flex', gap: '4px', marginTop: '6px', flexWrap: 'wrap' }}>
          {matchedActiveGroups.map(group => (
            <span
              key={group.id}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '3px',
                padding: '2px 8px',
                borderRadius: '10px',
                fontSize: '11px',
                fontWeight: 600,
                background: '#dcfce7',
                color: '#166534',
              }}
            >
              ✓ {group.label} 처치 가능
            </span>
          ))}
        </div>
      )}

      <div style={{ display: 'flex', gap: '6px', marginTop: '6px', fontSize: '12px', color: '#6b7280', flexWrap: 'wrap' }}>
        {showBeds ? (
          <span>응급실 병상 <strong style={{ color: '#111827' }}>{hospital.availableBeds}</strong>개</span>
        ) : (
          <span style={{ color: '#9ca3af' }}>병상 정보 없음</span>
        )}
        {relativeTime && (
          <span title={BED_UPDATE_HELP} style={{ color: hospital.stale ? '#b45309' : '#9ca3af', cursor: 'help' }}>
            · 병상 업데이트 {relativeTime}
          </span>
        )}
      </div>

      {hospital.blockMessages?.length > 0 && (
        <div style={{ marginTop: '8px', padding: '6px 10px', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '6px' }}>
          {hospital.blockMessages.map((m, idx) => (
            <p key={`${m.diseaseTypeName ?? ''}-${m.messageType ?? ''}-${m.message ?? ''}-${idx}`} style={{ fontSize: '12px', color: '#991b1b', lineHeight: 1.4 }}>
              ⚠️ <strong>{m.diseaseTypeName || m.messageType || '수용 제한'}</strong>
              {m.message && <span> · {m.message}</span>}
            </p>
          ))}
        </div>
      )}

      <div style={{ display: 'flex', gap: '8px', marginTop: '10px', alignItems: 'stretch' }}>
        {hospital.phone && (
          <a
            href={`tel:${hospital.phone}`}
            onClick={e => e.stopPropagation()}
            style={{
              flex: hasCoords ? '1 1 auto' : '0 0 auto',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              padding: '10px 14px',
              borderRadius: '8px',
              background: '#2563eb',
              color: '#fff',
              fontSize: '14px',
              fontWeight: 700,
              textDecoration: 'none',
              whiteSpace: 'nowrap',
            }}
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
            style={{
              flex: '0 0 auto',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              padding: '10px 14px',
              borderRadius: '8px',
              border: '1px solid #d1d5db',
              color: '#374151',
              fontSize: '13px',
              fontWeight: 600,
              textDecoration: 'none',
              whiteSpace: 'nowrap',
            }}
          >
            🧭 길찾기
          </a>
        )}
      </div>
    </div>
  )
}
