# FindEr - Claude Code Guide

## 프로젝트 개요

응급실 지도 탐색 서비스. 내 위치 기반으로 실시간 가용 가능한 가장 가까운 응급실을 지도로 찾아주는 서비스.

---

## 아키텍처

각 도메인을 독립 패키지로 관리하고, 각 패키지 안에서 레이어를 분리한다.

### 패키지 구조

```
com.finder
├── hospital/                        # 병원 도메인
│   ├── client/                      # 외부 API client (E-Gen, safetydata)
│   ├── controller/
│   ├── domain/                      # HospitalInfo, BedSnapshot 등
│   ├── dto/
│   └── service/                     # 유스케이스, 캐시, 스케줄러
└── common/
    ├── config/                      # RestTemplate, CORS 등 공통 설정
    ├── exception/                   # 커스텀 예외
    ├── response/                    # 표준 응답 포맷
    └── GlobalExceptionHandler.java
```

### 레이어 의존 방향

```
controller → service → repository → domain
```

역방향 의존 금지. controller가 repository를 직접 호출하지 않는다.

---

## 코딩 컨벤션

### DTO

- 모든 DTO는 Java `record`로 작성한다.
- Request, Response를 명확히 구분한다.

```java
public record HospitalSearchRequest(
        @NotNull Double lat,
        @NotNull Double lng,
        @Positive double radiusKm
) {}
```

### 엔티티

- 생성자 직접 노출 금지. 정적 팩토리 메서드(`create`) 사용.
- setter 금지. 상태 변경은 의미 있는 메서드명으로.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수.
- 현재 영속 엔티티는 없다. 새 엔티티를 추가할 때 위 규칙을 따른다.

### 예외 처리

커스텀 예외 + `@RestControllerAdvice` 전역 핸들러 패턴 사용.

**커스텀 예외**: 상황별 명확한 예외 클래스 정의.
```
common/exception/
├── NotFoundException.java        # 리소스 없음 (404)
├── UnauthorizedException.java    # 인증 실패 (401)
└── InvalidRequestException.java  # 잘못된 요청 (400)
```

**표준 에러 응답 포맷**:
```json
{
  "code": "NOT_FOUND",
  "message": "존재하지 않는 병원입니다."
}
```

각 레이어에서 try-catch 금지. 예외는 전역 핸들러에서만 처리.

### 주석

- 클래스, public 메서드에 한 줄 JavaDoc 작성.
- 비즈니스 규칙이나 외부 제약이 있는 코드에만 인라인 주석 작성.
- 자명한 코드에 주석 금지.

```java
/** 좌표/반경에 해당하는 병원 목록을 거리·가용 상태 기준으로 정렬해 반환한다. */
public HospitalListResponse getHospitals(double lat, double lng, double radiusKm) { ... }
```

---

## 테스트

### 작성 대상

- **domain**: 비즈니스 로직 단위 테스트 (JUnit5, 외부 의존 없음)
- **service**: 서비스 레이어 단위 테스트 (Mockito로 repository mock)
- **controller**: 슬라이스 테스트 (@WebMvcTest)

### 네이밍

```java
@Test
void 반경_내_병원이_없으면_빈_목록을_반환한다() { ... }

@Test
void 갱신_시각이_30분_초과인_병상정보는_UNKNOWN으로_분류한다() { ... }
```

### 원칙

- 테스트는 구현이 아닌 행동(behavior)을 검증한다.
- given / when / then 구조로 작성한다.

---

## Git 전략

### 브랜치 (GitHub Flow)

```
main          → 항상 배포 가능 상태. push 시 자동 배포됨.
feature/xxx   → 기능 단위 개발 브랜치. 완성 후 main에 머지.
fix/xxx       → 버그 수정 브랜치.
```

**브랜치 네이밍 예시**:
- `feature/hospital-search-api`
- `feature/map-center-marker`
- `fix/bed-cache-stale-threshold`

### 커밋 메시지

한글로 작성한다. 타입 접두사를 붙인다.

```
feat: 지도 중심 재검색 기능 구현
fix: 토큰 중복 생성 버그 수정
refactor: 병원 서비스 레이어 분리
test: 병원 서비스 단위 테스트 추가
chore: GitHub Actions Node.js 버전 업데이트
```

---

## API 규칙

- Base URL: `/api/v1`
- 성공 응답: HTTP 상태코드로 구분 (200, 201, 204)
- 에러 응답: 항상 `{ code, message }` 포맷

---

## 외부 API

- **E-Gen 응급의료기관 정보** (`getEgytLcinfoInqire`): 위치 기반 병원 목록 조회. 매 요청 호출.
- **safetydata.go.kr 실시간 병상정보** (`DSSP-IF-00242`): 전국 425개 병원 병상 데이터. 3분 주기로 메모리 캐시 갱신, 사용자 요청 시 캐시 룩업.
- **Kakao Maps**: 프론트엔드에서 직접 호출.

---

## 데이터 표시 정책

응급의료 데이터는 신뢰도가 사용자 의사결정에 직결되므로 다음 규칙을 따른다.

- **가용 병상 수 직접 노출**: `availableBeds` 필드를 카드 UI에 숫자로 표시한다. (예: "병상 6개")
- **갱신 시각 함께 표시**: `updatedAt`을 상대 시간 형식으로 같이 노출한다. (예: "5분 전 갱신")
- **UNKNOWN 처리**: 백엔드에서 `availableEmergencyBeds`가 음수이거나 갱신이 30분 초과한 경우 status를 UNKNOWN으로 산출한다. UI에서는 UNKNOWN 상태일 때 병상 수 숫자를 숨기고 "정보 없음"으로 표시한다.
- **상태 카테고리**: GREEN(여유, ≥4) / YELLOW(보통, 1~3) / RED(혼잡, 0) / UNKNOWN(정보 없음). 색상 마커와 배지에 사용.

---

## 환경변수

민감 정보는 환경변수로 관리. 코드에 직접 삽입 금지.

| 변수명 | 용도 |
|---|---|
| `DB_URL` | MySQL 연결 URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `EGEN_API_KEY` | E-Gen 공공데이터 API 키 |
| `BED_API_KEY` | safetydata.go.kr 실시간 병상정보 API 키 |
