/** 증상 그룹 → 매칭되는 mkioskty 코드 집합 매핑. AND 검색 시 그룹 단위로 OR. */
export const TREATMENT_GROUPS = [
  { id: 'cardiac',    label: '심근경색',         codes: ['mkioskty1'] },
  { id: 'stroke',     label: '뇌졸중',           codes: ['mkioskty2', 'mkioskty3', 'mkioskty4'] },
  { id: 'aorta',      label: '대동맥응급',       codes: ['mkioskty5', 'mkioskty6'] },
  { id: 'abdomen',    label: '복부응급수술',     codes: ['mkioskty9'] },
  { id: 'endoscopy',  label: '응급내시경',       codes: ['mkioskty11', 'mkioskty13'] },
  { id: 'obgyn',      label: '산부인과응급',     codes: ['mkioskty16', 'mkioskty17', 'mkioskty18'] },
  { id: 'burn',       label: '중증화상',         codes: ['mkioskty19'] },
  { id: 'limb',       label: '사지접합',         codes: ['mkioskty20', 'mkioskty21'] },
  { id: 'dialysis',   label: '응급투석',         codes: ['mkioskty22', 'mkioskty23'] },
  { id: 'psych',      label: '정신과(폐쇄병동)', codes: ['mkioskty24'] },
  { id: 'eye',        label: '안과응급',         codes: ['mkioskty25'] },
  { id: 'pediatric',  label: '소아응급',         codes: ['mkioskty10', 'mkioskty12', 'mkioskty14', 'mkioskty15', 'mkioskty27'] },
]

/** 병원의 가용 시술 코드 집합이 선택된 모든 그룹을 만족하는지 판정. */
export function matchesAllGroups(hospitalCodes, selectedGroupIds) {
  if (selectedGroupIds.length === 0) return true
  if (!hospitalCodes || hospitalCodes.length === 0) return false
  const set = new Set(hospitalCodes)
  return selectedGroupIds.every(groupId => {
    const group = TREATMENT_GROUPS.find(g => g.id === groupId)
    return group && group.codes.some(code => set.has(code))
  })
}

export default function TreatmentFilter({ selected, onChange }) {
  const toggle = (id) => {
    onChange(selected.includes(id) ? selected.filter(s => s !== id) : [...selected, id])
  }

  return (
    <div style={{ padding: '12px 16px', borderBottom: '1px solid #e5e7eb' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
        <p style={{ fontSize: '12px', color: '#9ca3af' }}>증상별 필터</p>
        {selected.length > 0 && (
          <button
            type="button"
            onClick={() => onChange([])}
            style={{ fontSize: '11px', color: '#3b82f6', background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
          >
            초기화
          </button>
        )}
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
        {TREATMENT_GROUPS.map(group => {
          const active = selected.includes(group.id)
          return (
            <button
              key={group.id}
              type="button"
              aria-pressed={active}
              onClick={() => toggle(group.id)}
              style={{
                padding: '4px 10px',
                fontSize: '12px',
                fontWeight: active ? 600 : 400,
                color: active ? '#fff' : '#374151',
                background: active ? '#dc2626' : '#fff',
                border: active ? 'none' : '1px solid #d1d5db',
                borderRadius: '14px',
                cursor: 'pointer',
              }}
            >
              {group.label}
            </button>
          )
        })}
      </div>
    </div>
  )
}
