import HospitalItem from './HospitalItem'
import HospitalDetailPanel from './HospitalDetailPanel'
import LocationSearch from './LocationSearch'
import TreatmentFilter from './TreatmentFilter'
import styles from './HospitalPanel.module.css'

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
        <div className={styles.panelHeader}>
          <p className={styles.panelTitle}>내 주변 응급실</p>
          {isCustom && customLabel && (
            <p className={styles.customLocationLabel}>📍 {customLabel} 기준</p>
          )}
        </div>

        <LocationSearch onLocate={onLocate} isCustom={isCustom} onResetToGps={onResetToGps} />

        <div className={styles.sliderSection}>
          <div className={styles.sliderHeader}>
            <p className={styles.sliderLabel}>검색 반경</p>
            <p className={styles.sliderValue}>{radius}km</p>
          </div>
          <input
            type="range"
            min="1"
            max="20"
            step="1"
            value={radius}
            onChange={e => onRadiusChange(Number(e.target.value))}
            aria-label="검색 반경"
            className={styles.rangeInput}
          />
          <div className={styles.rangeLimits}>
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
        <div className={styles.resultsHeader}>
          <p className={styles.resultsTitle}>응급실 정보</p>
          <p className={styles.resultsCount}>
            {loading ? '검색 중...' : countText}
          </p>
        </div>

        <div className={styles.scrollableContent}>
          <HospitalDetailPanel
            hospital={selectedHospitalDetail ?? selectedHospital}
            loading={detailLoading}
            error={detailError}
            onClose={onCloseDetail}
          />

          {selectedHospital && (
            <div className={styles.sectionLabel}>
              <p className={styles.sectionLabelText}>주변 응급실 목록</p>
            </div>
          )}

          {loading ? (
            <div className={styles.emptyState}>
              불러오는 중...
            </div>
          ) : hospitals.length === 0 ? (
            <div className={styles.emptyState}>
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
