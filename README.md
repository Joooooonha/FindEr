# FindEr — 응답하라 응급실

지금 나를 받아줄 수 있는 가장 가까운 응급실을 지도로 찾아주는 서비스.

## 핵심 기능

1. **지도 기반 응급실 탐색** — 내 위치 기반, 가용 상태(🟢🟡🔴) 표시
2. **병원 상세 정보** — 진료 가능 과목, 수술 가능 여부, 전화 연결

## 타겟 유저

119를 부르지 않고 **직접 이동하는 준응급 상황**의 환자·보호자
- 야간 고열 아이를 데려가는 부모
- 혼자 갑자기 아픈 1인 가구
- 응급 상황에서 병원을 찾는 주변인

## 기술 스택

| 영역 | 기술 |
|---|---|
| Backend | Spring Boot |
| Database | MySQL |
| CI/CD | GitHub Actions |
| Server | AWS EC2 |
| Frontend | 미정 |
| Map | Kakao Maps API |
| Data | E-Gen 공공데이터 API |

## 문서

- [아키텍처](docs/architecture.md)
- [DB 스키마](docs/db-schema.md)
- [API 명세](docs/api-spec.md)
- [외부 API 연동](docs/external-api.md)
