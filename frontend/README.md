# FindEr Frontend

FindEr의 지도 탐색 UI입니다. 프로젝트 배경과 전체 아키텍처는 [루트 README](../README.md)를 먼저 확인하세요.

## 기술 구성

- React 19
- Vite 8
- React Router 7
- Kakao Maps JavaScript SDK
- ESLint 10

## 화면 책임

| 경로 | 역할 |
|---|---|
| `src/pages/MapPage.jsx` | 위치·반경·필터·정렬·상세 조회 상태 조합 |
| `src/components/KakaoMap.jsx` | 지도 SDK 초기화, 마커와 병상 라벨 렌더링 |
| `src/components/HospitalPanel.jsx` | 검색·필터·목록 패널 구성 |
| `src/components/HospitalItem.jsx` | 병원 요약, 갱신 시각, 수용 제한, 길찾기 |
| `src/components/HospitalDetailPanel.jsx` | 병상·장비·처치 상세 정보 |
| `src/components/TreatmentFilter.jsx` | 증상 그룹과 `mkioskty` 코드 매핑 |
| `src/api/hospital.js` | 백엔드 목록·상세 API 호출 |

## 필터와 정렬

- 반경: 1–20km
- 정렬: 거리, 가용 병상, 갱신 시각
- 조건: 가용 병상만, 갱신 시간 범위, 증상 그룹
- 증상 그룹 여러 개를 선택하면 그룹 간에는 AND, 한 그룹 안의 처치 코드는 OR로 매칭합니다.

`stale: true`인 병상 데이터는 지도, 목록, 상세 화면에서 숫자를 표시하지 않고 가용 병상 필터에서도 제외합니다.

## 로컬 실행

`.env.local`을 만듭니다.

```dotenv
VITE_KAKAO_JS_KEY=...
VITE_API_BASE_URL=http://localhost:8080
```

```bash
npm install
npm run dev
```

개발 서버의 `/api` 요청은 `vite.config.js`에 따라 기본적으로 `http://localhost:8080`으로 프록시됩니다.

## 검증

```bash
npm run lint
npm run build
```

Kakao Maps JavaScript 키에는 로컬·배포 도메인을 Kakao Developers에서 등록해야 합니다. 키 값을 코드나 문서에 직접 커밋하지 마세요.
