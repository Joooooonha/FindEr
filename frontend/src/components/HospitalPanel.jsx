import HospitalItem from './HospitalItem'
import HospitalDetailPanel from './HospitalDetailPanel'
import LocationSearch from './LocationSearch'
import TreatmentFilter from './TreatmentFilter'

export default function HospitalPanel({
  hospitals,
  totalCount,
  loading,
  radius,
  onRadiusChange,
  selectedHospital,
  selectedHospitalDetail,
  detailLoading,
  detailError,
  onSelect,
  onCloseDetail,
  onLocate,
  isCustom,
  customLabel,
  onResetToGps,
  selectedTreatments,
  onTreatmentsChange,
  children,
}) {
  const filtered = selectedTreatments?.length > 0
  const countText = filtered
    ? `${hospitals.length}개 매칭 (전체 ${totalCount}개 중)`
    : `${hospitals.length}개 응급실`

  return (
    <div className="finder-map-layout">
      <aside className="finder-control-panel">
        <div style={{ padding: '18px 18px 16px', borderBottom: '1px solid #e5e7eb' }}>
          <p style={{ fontSize: '16px', fontWeight: 700, color: '#111827' }}>내 주변 응급실</p>
          {isCustom && customLabel && (
            <p style={{ fontSize: '12px', color: '#3b82f6', marginTop: '4px' }}>📍 {customLabel} 기준</p>
          )}
        </div>

        <LocationSearch onLocate={onLocate} isCustom={isCustom} onResetToGps={onResetToGps} />

        <div style={{ padding: '14px 18px', borderBottom: '1px solid #e5e7eb' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <p style={{ fontSize: '12px', color: '#9ca3af' }}>검색 반경</p>
            <p style={{ fontSize: '13px', color: '#3b82f6', fontWeight: 600 }}>{radius}km</p>
          </div>
          <input
            type="range"
            min="1"
            max="20"
            step="1"
            value={radius}
            onChange={e => onRadiusChange(Number(e.target.value))}
            aria-label="검색 반경"
            style={{ width: '100%', accentColor: '#3b82f6', cursor: 'pointer' }}
          />
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: '#9ca3af', marginTop: '2px' }}>
            <span>1km</span>
            <span>20km</span>
          </div>
        </div>

        <TreatmentFilter selected={selectedTreatments ?? []} onChange={onTreatmentsChange} />
      </aside>

      <main className="finder-map-pane">
        {children}
      </main>

      <aside className="finder-results-panel">
        <div style={{ padding: '16px 18px', borderBottom: '1px solid #e5e7eb', background: '#fff' }}>
          <p style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>응급실 정보</p>
          <p style={{ marginTop: '3px', fontSize: '12px', color: '#6b7280' }}>
            {loading ? '검색 중...' : countText}
          </p>
        </div>

        <div style={{ flex: 1, overflowY: 'auto' }}>
          <HospitalDetailPanel
            hospital={selectedHospitalDetail ?? selectedHospital}
            loading={detailLoading}
            error={detailError}
            onClose={onCloseDetail}
          />

          {selectedHospital && (
            <div style={{ padding: '2px 18px 10px' }}>
              <p style={{ fontSize: '12px', color: '#9ca3af' }}>주변 응급실 목록</p>
            </div>
          )}

          {loading ? (
            <div style={{ padding: '40px 18px', textAlign: 'center', color: '#9ca3af', fontSize: '14px' }}>
              불러오는 중...
            </div>
          ) : hospitals.length === 0 ? (
            <div style={{ padding: '40px 18px', textAlign: 'center', color: '#9ca3af', fontSize: '14px' }}>
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
      </aside>
    </div>
  )
}
