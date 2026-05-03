# FindEr - Claude Code Guide

## 프로젝트 개요

응급실 지도 탐색 서비스. 내 위치 기반으로 실시간 가용 가능한 가장 가까운 응급실을 지도로 찾아주는 서비스.

---

## 아키텍처

각 도메인을 독립 패키지로 관리하고, 각 패키지 안에서 레이어를 분리한다.

### 패키지 구조

```
com.finder
├── card/                            # 응급카드 도메인
│   ├── domain/
│   │   └── EmergencyCard.java       # JPA 엔티티
│   ├── repository/
│   │   └── EmergencyCardRepository.java
│   ├── service/
│   │   └── EmergencyCardService.java
│   ├── controller/
│   │   └── EmergencyCardController.java
│   └── dto/
│       ├── CardCreateRequest.java
│       ├── CardUpdateRequest.java
│       ├── CardCreateResponse.java
│       └── CardResponse.java
├── hospital/                        # 병원 도메인
│   ├── domain/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── dto/
└── common/
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
public record CardCreateRequest(
        @NotBlank String name,
        LocalDate birthDate,
        @NotBlank @Size(min = 4, max = 8) String pin
) {}
```

### 엔티티

- 생성자 직접 노출 금지. 정적 팩토리 메서드(`create`) 사용.
- setter 금지. 상태 변경은 의미 있는 메서드명으로.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수.

```java
// 올바른 예
public static EmergencyCard create(String token, CardCreateRequest req, String pinHash) { ... }
public void update(CardUpdateRequest req) { ... }

// 금지
public void setName(String name) { ... }
```

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
  "message": "존재하지 않는 카드입니다."
}
```

각 레이어에서 try-catch 금지. 예외는 전역 핸들러에서만 처리.

### 주석

- 클래스, public 메서드에 한 줄 JavaDoc 작성.
- 비즈니스 규칙이나 외부 제약이 있는 코드에만 인라인 주석 작성.
- 자명한 코드에 주석 금지.

```java
/** PIN 검증 실패 시 카드 존재 여부 노출 방지를 위해 동일한 예외를 반환한다. */
public void deleteCard(String token, String pin) { ... }
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
void 토큰이_존재하지_않으면_NotFoundException을_던진다() { ... }

@Test
void PIN이_틀리면_카드를_삭제하지_않는다() { ... }
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
- `feature/emergency-card-api`
- `feature/hospital-search-api`
- `fix/card-token-duplicate`

### 커밋 메시지

한글로 작성한다. 타입 접두사를 붙인다.

```
feat: 응급카드 생성 API 구현
fix: 토큰 중복 생성 버그 수정
refactor: 병원 서비스 레이어 분리
test: 응급카드 서비스 단위 테스트 추가
chore: GitHub Actions Node.js 버전 업데이트
```

---

## API 규칙

- Base URL: `/api/v1`
- 성공 응답: HTTP 상태코드로 구분 (200, 201, 204)
- 에러 응답: 항상 `{ code, message }` 포맷
- PIN은 요청 헤더 `X-Card-Pin`으로 전달

---

## 외부 API

- **E-Gen 실시간 가용병상**: API 키 승인 대기 중. 현재 `UNKNOWN` 상태로 Mock 응답.
- **Kakao Maps**: 프론트엔드에서 직접 호출.

---

## 환경변수

민감 정보는 환경변수로 관리. 코드에 직접 삽입 금지.

| 변수명 | 용도 |
|---|---|
| `DB_URL` | MySQL 연결 URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `APP_BASE_URL` | 응급카드 공유 URL 생성용 |
| `EGEN_API_KEY` | E-Gen 공공데이터 API 키 |
