import HospitalItem from './HospitalItem'
import LocationSearch from './LocationSearch'
import TreatmentFilter from './TreatmentFilter'
import styles from './HospitalPanel.module.css'

function formatLastFetchedAt(value) {
  if (!value) return '아직 조회 전'
  return value.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function HospitalList({ hospitals, selectedHospital, expandedHospitalIds, hospitalDetails, detailLoadingById, detailErrorById, onSelect, onCloseDetail }) {
  return hospitals.map(h => (
    <HospitalItem
      key={h.id}
      hospital={h}
      isSelected={selectedHospital?.id === h.id}
      isExpanded={expandedHospitalIds.includes(String(h.id))}
      detail={hospitalDetails[String(h.id)]}
      detailLoading={Boolean(detailLoadingById[String(h.id)])}
      detailError={detailErrorById[String(h.id)]}
      onClick={() => onSelect(h)}
      onCloseDetail={() => onCloseDetail(String(h.id))}
    />
  ))
}

export default function HospitalPanel({
  hospitals,
  matchedHospitals = [],
  noDataHospitals = [],
  unavailableHospitals = [],
  hasTreatmentFilter = false,
  totalCount,
  loading,
  radius,
  onRadiusChange,
  selectedHospital,
  onSelect,
  onCloseDetail,
  onLocate,
  isCustom,
  customLabel,
  onResetToGps,
  selectedTreatments,
  onTreatmentsChange,
  sortBy,
  onSortChange,
  onlyAvailableBeds,
  onOnlyAvailableBedsChange,
  updateWindow,
  onUpdateWindowChange,
  lastFetchedAt,
  expandedHospitalIds,
  hospitalDetails,
  detailLoadingById,
  detailErrorById,
  children,
}) {
  const countText = hasTreatmentFilter
    ? `수용 가능 ${matchedHospitals.length}개 · 전체 ${totalCount}개`
    : hospitals.length !== totalCount
      ? `${hospitals.length}개 표시 (전체 ${totalCount}개 중)`
      : `${hospitals.length}개 응급실`

  const listProps = {
    selectedHospital,
    expandedHospitalIds,
    hospitalDetails,
    detailLoadingById,
    detailErrorById,
    onSelect,
    onCloseDetail,
  }

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

        <div className={styles.searchMetaBox}>
          <p className={styles.searchMetaTitle}>
            {isCustom && customLabel ? `${customLabel} 기준` : '현재 위치 기준'}
          </p>
          <p className={styles.searchMetaText}>
            반경 {radius}km · {countText} · 마지막 조회 {formatLastFetchedAt(lastFetchedAt)}
          </p>
        </div>

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

        <div className={styles.resultsControls}>
          <label className={styles.controlField}>
            <span className={styles.controlLabel}>정렬</span>
            <select
              value={sortBy}
              onChange={e => onSortChange(e.target.value)}
              className={styles.selectControl}
            >
              <option value="distance">거리순</option>
              <option value="beds">병상 많은 순</option>
              <option value="updated">업데이트 최신순</option>
            </select>
          </label>

          <label className={styles.controlField}>
            <span className={styles.controlLabel}>최근 업데이트</span>
            <select
              value={updateWindow}
              onChange={e => onUpdateWindowChange(e.target.value)}
              className={styles.selectControl}
            >
              <option value="all">전체</option>
              <option value="1">1시간 이내</option>
              <option value="3">3시간 이내</option>
              <option value="6">6시간 이내</option>
              <option value="12">12시간 이내</option>
            </select>
          </label>

          <label className={styles.checkboxControl}>
            <input
              type="checkbox"
              checked={onlyAvailableBeds}
              onChange={e => onOnlyAvailableBedsChange(e.target.checked)}
            />
            <span>가용 병상 있음</span>
          </label>
        </div>

        <div className={styles.scrollableContent}>
          {loading ? (
            <div className={styles.emptyState}>
              불러오는 중...
            </div>
          ) : !hasTreatmentFilter ? (
            hospitals.length === 0 ? (
              <div className={styles.emptyState}>주변 응급실이 없습니다</div>
            ) : (
              <HospitalList hospitals={hospitals} {...listProps} />
            )
          ) : (matchedHospitals.length + noDataHospitals.length + unavailableHospitals.length) === 0 ? (
            <div className={styles.emptyState}>조건에 맞는 응급실이 없습니다</div>
          ) : (
            <>
              <div className={styles.sectionTitle}>
                ✅ 수용 가능 <span className={styles.sectionCount}>{matchedHospitals.length}</span>
              </div>
              {matchedHospitals.length === 0 ? (
                <p className={styles.sectionHint}>선택한 증상을 지금 수용 가능하다고 보고한 응급실이 없습니다.</p>
              ) : (
                <HospitalList hospitals={matchedHospitals} {...listProps} />
              )}

              {noDataHospitals.length > 0 && (
                <>
                  <div className={styles.sectionTitle}>
                    ❔ 정보 미보고 <span className={styles.sectionCount}>{noDataHospitals.length}</span>
                  </div>
                  <p className={styles.sectionHint}>중증질환 수용정보를 보고하지 않은 응급실입니다. 전화로 확인이 필요합니다.</p>
                  <div className={styles.dimmedGroup}>
                    <HospitalList hospitals={noDataHospitals} {...listProps} />
                  </div>
                </>
              )}

              {unavailableHospitals.length > 0 && (
                <>
                  <div className={styles.sectionTitle}>
                    ⛔ 수용 불가 <span className={styles.sectionCount}>{unavailableHospitals.length}</span>
                  </div>
                  <p className={styles.sectionHint}>선택한 증상을 현재 수용 불가로 보고한 응급실입니다.</p>
                  <div className={styles.dimmedGroup}>
                    <HospitalList hospitals={unavailableHospitals} {...listProps} />
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </aside>
    </div>
  )
}
