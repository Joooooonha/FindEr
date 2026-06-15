import HelpBadge from './HelpBadge'
import { TREATMENT_GROUPS } from './treatmentGroups'
import styles from './TreatmentFilter.module.css'

const FILTER_HELP =
  '선택한 증상을 지금 수용 가능하다고 보고한 응급실을 우선 표시합니다. ' +
  '병원이 중증질환 수용정보를 보고하지 않은 경우 "정보 미보고"로 따로 묶어 보여줍니다.'

export default function TreatmentFilter({ selected, onChange }) {
  const toggle = (id) => {
    onChange(selected.includes(id) ? selected.filter(s => s !== id) : [...selected, id])
  }

  return (
    <div className={styles.section}>
      <div className={styles.header}>
        <span className={styles.titleRow}>
          <span className={styles.title}>증상별 필터</span>
          <HelpBadge label={FILTER_HELP} />
        </span>
        {selected.length > 0 && (
          <button type="button" onClick={() => onChange([])} className={styles.reset}>
            초기화
          </button>
        )}
      </div>
      <p className={styles.help}>증상을 고르면 지금 수용 가능한 응급실을 위로 모아 보여줍니다.</p>
      <div className={styles.chips}>
        {TREATMENT_GROUPS.map(group => {
          const active = selected.includes(group.id)
          return (
            <button
              key={group.id}
              type="button"
              aria-pressed={active}
              title={group.desc}
              onClick={() => toggle(group.id)}
              className={`${styles.chip}${active ? ` ${styles.chipActive}` : ''}`}
            >
              {group.label}
            </button>
          )
        })}
      </div>
    </div>
  )
}
