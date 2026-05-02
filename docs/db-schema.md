# DB 스키마

## emergency_card (응급 카드)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| token | VARCHAR(8) UNIQUE | 공개 URL용 고유 토큰 |
| name | VARCHAR(50) | 이름 |
| birth_date | DATE | 생년월일 |
| blood_type | VARCHAR(5) | 혈액형 (A+, B-, O+, AB+ 등) |
| allergies | TEXT | 알레르기 (쉼표 구분) |
| medications | TEXT | 복용 중인 약 (쉼표 구분) |
| conditions | TEXT | 기저질환 (쉼표 구분) |
| surgeries | TEXT | 수술 이력 |
| guardian_name | VARCHAR(50) | 보호자 이름 |
| guardian_phone | VARCHAR(20) | 보호자 연락처 |
| is_pregnant | BOOLEAN | 임신 여부 |
| pin_hash | VARCHAR(255) | 카드 수정용 PIN (bcrypt) |
| created_at | DATETIME | |
| updated_at | DATETIME | |

## 비고

- 병원 정보는 DB에 저장하지 않음 → E-Gen API 실시간 호출
- 향후 조회 성능 개선 필요 시 hospital 테이블 추가 검토
