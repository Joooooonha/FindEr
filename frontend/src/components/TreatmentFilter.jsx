import { useState } from 'react'

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

/** 병원이 실제로 지원하는 증상 그룹 목록을 반환한다. 카드/상세에서 매칭 근거를 보여주는 데 쓴다. */
export function getMatchedGroups(hospitalCodes) {
  if (!hospitalCodes || hospitalCodes.length === 0) return []
  const set = new Set(hospitalCodes)
  return TREATMENT_GROUPS.filter(group => group.codes.some(code => set.has(code)))
}

// 12개를 한 번에 나열하던 걸 빈도/영역 기준 3그룹(그룹당 ≤4개)으로 묶고,
// 상대적으로 드문 3개는 '더 보기' 뒤로 숨겨 초기 인지부하를 낮춘다.
const TREATMENT_CATEGORIES = [
  { title: '심뇌혈관 응급', groupIds: ['cardiac', 'stroke', 'aorta'] },
  { title: '외상·응급수술', groupIds: ['abdomen', 'burn', 'endoscopy'] },
  { title: '산부인과·소아·투석', groupIds: ['obgyn', 'pediatric', 'dialysis'] },
]
const MORE_TREATMENT_GROUP_IDS = ['limb', 'psych', 'eye']

function TreatmentPill({ group, active, onClick }) {
  return (
    <button
      type="button"
      aria-pressed={active}
      onClick={onClick}
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
}

export default function TreatmentFilter({ selected, onChange }) {
  const [showMore, setShowMore] = useState(false)
  const toggle = (id) => {
    onChange(selected.includes(id) ? selected.filter(s => s !== id) : [...selected, id])
  }
  const groupById = (id) => TREATMENT_GROUPS.find(g => g.id === id)
  // 숨겨둔 항목 중 이미 선택된 게 있으면(예: 이전 세션 상태 복원) 계속 보이게 한다.
  const hasSelectedHiddenGroup = MORE_TREATMENT_GROUP_IDS.some(id => selected.includes(id))

  return (
    <div style={{ padding: '12px 16px', borderBottom: '1px solid #e5e7eb' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
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

      {TREATMENT_CATEGORIES.map(category => (
        <div key={category.title} style={{ marginBottom: '10px' }}>
          <p style={{ fontSize: '11px', fontWeight: 600, color: '#9ca3af', marginBottom: '5px' }}>
            {category.title}
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
            {category.groupIds.map(id => (
              <TreatmentPill
                key={id}
                group={groupById(id)}
                active={selected.includes(id)}
                onClick={() => toggle(id)}
              />
            ))}
          </div>
        </div>
      ))}

      {(showMore || hasSelectedHiddenGroup) && (
        <div style={{ marginBottom: '4px' }}>
          <p style={{ fontSize: '11px', fontWeight: 600, color: '#9ca3af', marginBottom: '5px' }}>
            그 외 증상
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
            {MORE_TREATMENT_GROUP_IDS.map(id => (
              <TreatmentPill
                key={id}
                group={groupById(id)}
                active={selected.includes(id)}
                onClick={() => toggle(id)}
              />
            ))}
          </div>
        </div>
      )}

      {!hasSelectedHiddenGroup && (
        <button
          type="button"
          onClick={() => setShowMore(v => !v)}
          style={{
            fontSize: '11px',
            fontWeight: 600,
            color: '#6b7280',
            background: 'none',
            border: 'none',
            padding: 0,
            cursor: 'pointer',
          }}
        >
          {showMore ? '간단히 보기 ▲' : `그 외 증상 더 보기 (${MORE_TREATMENT_GROUP_IDS.length}) ▼`}
        </button>
      )}
    </div>
  )
}
