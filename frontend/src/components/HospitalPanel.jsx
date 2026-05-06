import HospitalItem from './HospitalItem'
import LocationSearch from './LocationSearch'
import TreatmentFilter from './TreatmentFilter'

export default function HospitalPanel({
  hospitals,
  totalCount,
  loading,
  radius,
  onRadiusChange,
  selectedHospital,
  onSelect,
  onLocate,
  isCustom,
  customLabel,
  onResetToGps,
  selectedTreatments,
  onTreatmentsChange,
}) {
  const filtered = selectedTreatments?.length > 0
  const countText = filtered
    ? `${hospitals.length}개 매칭 (전체 ${totalCount}개 중)`
    : `${hospitals.length}개 응급실`

  return (
    <div style={{
      width: '340px',
      minWidth: '340px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      background: '#fff',
      boxShadow: '2px 0 8px rgba(0,0,0,0.08)',
      zIndex: 10,
    }}>
      {/* 패널 타이틀 */}
      <div style={{ padding: '16px', borderBottom: '1px solid #e5e7eb' }}>
        <p style={{ fontSize: '14px', fontWeight: 600, color: '#111827' }}>내 주변 응급실</p>
        {isCustom && customLabel && (
          <p style={{ fontSize: '12px', color: '#3b82f6', marginTop: '2px' }}>📍 {customLabel} 기준</p>
        )}
      </div>

      {/* 위치 검색 */}
      <LocationSearch onLocate={onLocate} isCustom={isCustom} onResetToGps={onResetToGps} />

      {/* 반경 선택 */}
      <div style={{ padding: '12px 16px', borderBottom: '1px solid #e5e7eb' }}>
        <p style={{ fontSize: '12px', color: '#9ca3af', marginBottom: '8px' }}>검색 반경</p>
        <div style={{ display: 'flex', gap: '6px' }}>
          {[3, 5, 10].map(r => (
            <button
              key={r}
              onClick={() => onRadiusChange(r)}
              style={{
                padding: '5px 14px',
                borderRadius: '20px',
                border: radius === r ? 'none' : '1px solid #d1d5db',
                background: radius === r ? '#3b82f6' : '#fff',
                color: radius === r ? '#fff' : '#374151',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: radius === r ? 600 : 400,
              }}
            >
              {r}km
            </button>
          ))}
        </div>
      </div>

      {/* 증상별 필터 */}
      <TreatmentFilter selected={selectedTreatments ?? []} onChange={onTreatmentsChange} />

      {/* 병원 수 */}
      <div style={{ padding: '10px 16px', background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
        <p style={{ fontSize: '12px', color: '#6b7280' }}>
          {loading ? '검색 중...' : countText}
        </p>
      </div>

      {/* 병원 목록 */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {loading ? (
          <div style={{ padding: '40px 16px', textAlign: 'center', color: '#9ca3af', fontSize: '14px' }}>
            불러오는 중...
          </div>
        ) : hospitals.length === 0 ? (
          <div style={{ padding: '40px 16px', textAlign: 'center', color: '#9ca3af', fontSize: '14px' }}>
            {filtered ? '선택한 시술이 가능한 응급실이 없습니다' : '주변 응급실이 없습니다'}
          </div>
        ) : (
          hospitals.map(h => (
            <HospitalItem
              key={h.id}
              hospital={h}
              isSelected={selectedHospital?.id === h.id}
              onClick={() => onSelect(h)}
            />
          ))
        )}
      </div>
    </div>
  )
}
