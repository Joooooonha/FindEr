/**
 * 증상 그룹 → 매칭되는 mkioskty 코드 집합 매핑과 분류 로직.
 * 컴포넌트와 분리해 Fast Refresh 경고 없이 MapPage 등에서 공유한다.
 * label: 증상/상황 라벨, desc: 어떤 상황에 고르는지 짧은 안내(필터 기준 명확화).
 * AND 검색 시 그룹 단위로 OR.
 */
export const TREATMENT_GROUPS = [
  { id: 'cardiac',   label: '심근경색',         desc: '가슴 통증·압박, 급성 심근경색 의심', codes: ['mkioskty1'] },
  { id: 'stroke',    label: '뇌졸중',           desc: '갑작스러운 마비·언어장애·심한 두통', codes: ['mkioskty2', 'mkioskty3', 'mkioskty4'] },
  { id: 'aorta',     label: '대동맥응급',       desc: '찢어지는 듯한 가슴·등 통증',         codes: ['mkioskty5', 'mkioskty6'] },
  { id: 'abdomen',   label: '복부응급수술',     desc: '급성 복통, 응급 개복수술 필요',      codes: ['mkioskty9'] },
  { id: 'endoscopy', label: '응급내시경',       desc: '토혈·혈변 등 소화관 출혈',           codes: ['mkioskty11', 'mkioskty13'] },
  { id: 'obgyn',     label: '산부인과응급',     desc: '임신 관련 응급, 분만·출혈',          codes: ['mkioskty16', 'mkioskty17', 'mkioskty18'] },
  { id: 'burn',      label: '중증화상',         desc: '광범위·심부 화상',                   codes: ['mkioskty19'] },
  { id: 'limb',      label: '사지접합',         desc: '절단된 손·발가락·팔다리 접합',       codes: ['mkioskty20', 'mkioskty21'] },
  { id: 'dialysis',  label: '응급투석',         desc: '신부전 등 응급 투석',                codes: ['mkioskty22', 'mkioskty23'] },
  { id: 'psych',     label: '정신과(폐쇄병동)', desc: '자·타해 위험 정신과 응급',           codes: ['mkioskty24'] },
  { id: 'eye',       label: '안과응급',         desc: '급성 시력저하·안구 외상',            codes: ['mkioskty25'] },
  { id: 'pediatric', label: '소아응급',         desc: '소아 중증 응급',                     codes: ['mkioskty10', 'mkioskty12', 'mkioskty14', 'mkioskty15', 'mkioskty27'] },
]

/** 선택된 그룹 중 병원이 충족하는 그룹 id 목록. 매칭 근거 배지 용도. */
export function matchedGroupIds(hospitalCodes, selectedGroupIds) {
  if (!hospitalCodes || hospitalCodes.length === 0) return []
  const set = new Set(hospitalCodes)
  return selectedGroupIds.filter(groupId => {
    const group = TREATMENT_GROUPS.find(g => g.id === groupId)
    return group && group.codes.some(code => set.has(code))
  })
}

/** 그룹 id 목록 → 라벨 목록. */
export function groupLabels(groupIds) {
  return groupIds
    .map(id => TREATMENT_GROUPS.find(g => g.id === id)?.label)
    .filter(Boolean)
}

/**
 * 병원을 선택된 증상 기준으로 3단 분류한다.
 * - matched: 선택한 모든 그룹을 수용 가능하다고 보고
 * - unavailable: 수용정보를 보고했으나 일부 그룹을 충족하지 못함(= 수용 불가)
 * - no_data: 수용정보 자체가 없음(= 정보 미보고). "수용 불가"와 구분해 숨기지 않는다.
 */
export function classifyHospital(hospitalCodes, selectedGroupIds) {
  if (selectedGroupIds.length === 0) return 'matched'
  if (!hospitalCodes || hospitalCodes.length === 0) return 'no_data'
  const set = new Set(hospitalCodes)
  const all = selectedGroupIds.every(groupId => {
    const group = TREATMENT_GROUPS.find(g => g.id === groupId)
    return group && group.codes.some(code => set.has(code))
  })
  return all ? 'matched' : 'unavailable'
}

/** 병원의 가용 시술 코드 집합이 선택된 모든 그룹을 만족하는지 판정. (기존 호환용) */
export function matchesAllGroups(hospitalCodes, selectedGroupIds) {
  return classifyHospital(hospitalCodes, selectedGroupIds) === 'matched'
}
