# 외부 API 연동

FindEr는 두 공공데이터 소스와 Kakao Maps JavaScript SDK를 사용합니다. API 키나 운영 주소는 저장소에 커밋하지 않고 환경변수로 주입합니다.

## E-Gen 응급의료정보 API

기본 URL: `http://apis.data.go.kr/B552657/ErmctInfoInqireService`

| 경로 | 용도 | 주요 결합 값 |
|---|---|---|
| `/getEgytLcinfoInqire` | 좌표 주변 기관 조회와 콜드 캐시 폴백 | `hpid` |
| `/getEgytBassInfoInqire` | 기관 기본정보 단건 조회 | `hpid` |
| `/getEmrrmRltmUsefulSckbdInfoInqire` | 응급실 운영 기관 `hpid` 화이트리스트 | `hpid` |
| `/getEmrrmSrsillDissMsgInqire` | 응급실·중증질환 수용 제한 메시지 | `hpid` |
| `/getSrsillDissAceptncPosblInfoInqire` | 중증질환·시술 수용 가능 코드 | `hpid`, `mkioskty1..28` |

공통 인증키는 `EGEN_API_KEY`로 주입합니다.

### 구현상 예외 처리

- 위치 조회는 `WGS84_LAT`, `WGS84_LON`과 페이지 정보를 전달합니다.
- 상세 조회는 `HPID`를 전달합니다.
- 중증질환 수용 가능 정보는 17개 시도를 `STAGE1`으로 순회하며 수집합니다.
- E-Gen은 항목이 하나일 때 객체, 여러 개일 때 배열을 반환할 수 있어 두 형태를 모두 파싱합니다.
- 인증·할당량 오류가 XML로 반환될 수 있어 JSON의 정상 0건 응답과 별도로 처리합니다.
- 기본정보 전체에는 일반 의원 등이 포함되므로, 실시간 가용병상 응답의 `hpid`를 응급실 기관 화이트리스트로 사용합니다.

## 안전데이터 실시간 병상정보 API

기본 URL: `https://www.safetydata.go.kr/V2/api/DSSP-IF-00242`

| 항목 | 구현 값 |
|---|---|
| 인증 환경변수 | `BED_API_KEY` |
| Method | `GET` |
| 주요 파라미터 | `serviceKey`, `pageNo`, `numOfRows`, `returnType=json` |
| 페이지 크기 | 500 |
| 갱신 주기 | 3분 |
| 재시도 | 일시 오류 시 최대 3회 |

응답의 `BFR_INST_ID`를 E-Gen의 `hpid`와 결합합니다. 주요 병상 필드는 `EMRO`, `OPRO`, `WARD`, `GNRL_ICU`, `NRVS_ICU`, `EMRGN_ICU`이며, `MDFCN_DT`를 데이터 갱신 시각으로 사용합니다.

## 데이터 품질 규칙

- 가용 응급실 병상이 음수이면 `UNKNOWN`입니다.
- 갱신 시각이 없거나 현재보다 30분을 **초과**해 오래됐으면 `UNKNOWN`입니다.
- `MDFCN_DT`는 한국 시각으로 해석해 서버 기본 타임존과 무관하게 비교합니다.
- `UNKNOWN` 데이터의 병상 수와 상세 수용 수치는 API와 UI에서 숨깁니다.
- 외부 API 갱신이 실패하거나 빈 결과를 반환해도 기존의 정상 캐시를 빈 값으로 덮어쓰지 않습니다.
- 이 규칙은 잘못된 확신을 줄이는 장치일 뿐 실제 수용 가능성을 검증하지는 못합니다.

## Kakao Maps JavaScript SDK

| 환경변수 | 용도 |
|---|---|
| `VITE_KAKAO_JS_KEY` | 브라우저 지도 렌더링과 장소 검색 |
| `VITE_API_BASE_URL` | 프론트엔드가 호출할 FindEr 백엔드 주소 |

현재 프론트엔드는 Kakao REST API 키를 사용하지 않습니다. `VITE_` 환경변수는 번들에 노출될 수 있으므로 JavaScript SDK 키에는 Kakao Developers에서 허용 도메인을 제한해야 합니다.
