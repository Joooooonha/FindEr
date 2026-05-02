# 외부 API 연동

## E-Gen (국립중앙의료원)

### API 목록

| API명 | 용도 | 승인 상태 |
|---|---|---|
| 전국 응급의료기관 정보 조회 서비스 | 병원명, 위치, 진료과목, 장비 | 확인 필요 |
| 전국 응급의료기관 실시간 가용병상 정보 | 실시간 병상 현황 | 대기 중 |

### 기관 기본정보 API

- **URL**: `http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytListInfoInqire`
- **Method**: GET
- **주요 파라미터**:
  - `serviceKey`: 인증키
  - `WGS84_LON`: 경도
  - `WGS84_LAT`: 위도
  - `pageNo`, `numOfRows`

### 실시간 가용병상 API

- **URL**: `http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire`
- **Method**: GET
- **주요 파라미터**:
  - `serviceKey`: 인증키
  - `STAGE1`: 시도
  - `pageNo`, `numOfRows`

### 데이터 품질 주의사항

- 일부 병원 수동 입력 → 음수 병상값 발생 가능
- 음수 또는 비정상 값은 `UNKNOWN` 처리
- 갱신 시각이 30분 이상 지난 경우 `UNKNOWN` 처리
- 병상 수 자체보다 GREEN/YELLOW/RED 범주로만 표시

---

## Kakao Maps

- **JavaScript 키**: 환경변수 `KAKAO_JS_KEY`
- **REST API 키**: 환경변수 `KAKAO_REST_KEY`
- **용도**:
  - JS 키: 프론트엔드 지도 렌더링
  - REST 키: 주소 → 좌표 변환 (필요 시)
- **일일 무료 한도**: 300,000건
