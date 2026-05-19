import { BED_UPDATE_HELP } from '../constants/hospitalConstants'
import HelpBadge from './HelpBadge'
import StatusBadge from './StatusBadge'

const BED_ROWS = [
  { key: 'availableBeds', label: '응급실 병상' },
  { key: 'operatingRooms', label: '수술실' },
  { key: 'generalWardBeds', label: '일반 입원실' },
  { key: 'generalIcuBeds', label: '일반 중환자실' },
  { key: 'neuroIcuBeds', label: '신경과 중환자실' },
  { key: 'emergencyIcuBeds', label: '응급전용 중환자실' },
]

const EQUIPMENT_ROWS = [
  { key: 'ctAvailable', label: 'CT' },
  { key: 'mriAvailable', label: 'MRI' },
  { key: 'ventilatorAvailable', label: '인공호흡기' },
  { key: 'surgeryAvailable', label: '응급 수술' },
]

function formatUpdatedAt(value) {
  if (!value) return '갱신 시각 없음'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('ko-KR', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function CapacityValue({ value }) {
  if (!Number.isInteger(value) || value < 0) {
    return <span style={{ color: '#9ca3af' }}>정보 없음</span>
  }
  return <strong style={{ color: '#111827' }}>{value}개</strong>
}

function AvailabilityValue({ value }) {
  return (
    <span style={{ color: value ? '#166534' : '#9ca3af', fontWeight: 600 }}>
      {value ? '가능' : '확인 필요'}
    </span>
  )
}

export default function HospitalDetailPanel({ hospital, loading, error, onClose }) {
  if (!hospital && !loading) return null

  return (
    <section style={{
      margin: '12px',
      padding: '14px',
      border: '1px solid #e5e7eb',
      borderRadius: '8px',
      background: '#fff',
      boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: '8px', alignItems: 'flex-start' }}>
        <div style={{ minWidth: 0 }}>
          <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '3px' }}>선택한 병원</p>
          <h2 style={{ fontSize: '16px', lineHeight: 1.35, color: '#111827' }}>
            {hospital?.name ?? '상세 정보 불러오는 중'}
          </h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label="상세 패널 닫기"
          style={{
            width: '28px',
            height: '28px',
            border: '1px solid #e5e7eb',
            borderRadius: '6px',
            background: '#fff',
            cursor: 'pointer',
            color: '#6b7280',
          }}
        >
          ×
        </button>
      </div>

      {loading && (
        <p style={{ marginTop: '12px', fontSize: '13px', color: '#6b7280' }}>상세 정보를 불러오는 중입니다.</p>
      )}

      {error && (
        <p style={{ marginTop: '12px', fontSize: '13px', color: '#b91c1c' }}>{error}</p>
      )}

      {hospital && !loading && (
        <>
          <div style={{ marginTop: '10px', display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            <StatusBadge status={hospital.status} />
            <span style={{ fontSize: '12px', color: hospital.stale ? '#b45309' : '#6b7280' }}>
              병상 현황 업데이트: {formatUpdatedAt(hospital.updatedAt)}
            </span>
            <HelpBadge label={BED_UPDATE_HELP} />
          </div>

          <div style={{ marginTop: '12px', fontSize: '13px', color: '#4b5563', lineHeight: 1.5 }}>
            <p>{hospital.address}</p>
            {hospital.phone && (
              <a href={`tel:${hospital.phone}`} style={{ color: '#2563eb', textDecoration: 'none' }}>
                {hospital.phone}
              </a>
            )}
          </div>

          <div style={{ marginTop: '14px' }}>
            <h3 style={{ fontSize: '13px', color: '#111827', marginBottom: '8px' }}>병상 현황</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              {BED_ROWS.map(row => (
                <div key={row.key} style={{ padding: '8px', background: '#f9fafb', borderRadius: '6px' }}>
                  <p style={{ fontSize: '11px', color: '#6b7280', marginBottom: '3px' }}>{row.label}</p>
                  <CapacityValue value={hospital[row.key]} />
                </div>
              ))}
            </div>
          </div>

          <div style={{ marginTop: '14px' }}>
            <h3 style={{ fontSize: '13px', color: '#111827', marginBottom: '8px' }}>장비·처치 가능 여부</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              {EQUIPMENT_ROWS.map(row => (
                <div key={row.key} style={{ padding: '8px', background: '#f9fafb', borderRadius: '6px' }}>
                  <p style={{ fontSize: '11px', color: '#6b7280', marginBottom: '3px' }}>{row.label}</p>
                  <AvailabilityValue value={Boolean(hospital[row.key])} />
                </div>
              ))}
            </div>
          </div>

          {hospital.blockMessages?.length > 0 && (
            <div style={{ marginTop: '14px' }}>
              <h3 style={{ fontSize: '13px', color: '#111827', marginBottom: '8px' }}>수용 제한 메시지</h3>
              <div style={{ display: 'grid', gap: '6px' }}>
                {hospital.blockMessages.slice(0, 5).map(message => (
                  <p
                    key={`${message.diseaseTypeName ?? 'unknown'}-${message.messageType ?? 'unknown'}-${message.message ?? ''}`}
                    style={{
                      padding: '8px',
                      borderRadius: '6px',
                      border: '1px solid #fecaca',
                      background: '#fef2f2',
                      color: '#991b1b',
                      fontSize: '12px',
                      lineHeight: 1.45,
                    }}
                  >
                    <strong>{message.diseaseTypeName || message.messageType || '제한'}</strong>
                    {message.message && <span> · {message.message}</span>}
                  </p>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </section>
  )
}
