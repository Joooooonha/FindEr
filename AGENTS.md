# FindEr Agent Context

이 문서는 Claude Code, Codex, Cursor 등 모든 AI 코딩 에이전트가 FindEr 레포에서 일관된 컨텍스트로 작업하기 위한 **메인 가이드**다.

- 추측 금지. 이 문서에 없는 결정을 새로 내리면 작업 결과나 PR 설명에 근거를 남긴다.
- 불확실한 항목은 `[확인 필요]`로 표시한다.
- 의료/응급 데이터는 사용자 판단에 직접 영향을 주므로, 최신성·출처·불확실성을 UI/API에서 숨기지 않는다.

---

## 0. 이 문서의 위치

- **이 파일이 메인 컨텍스트다.** 어떤 AI 에이전트(Claude Code, Codex, Cursor, Aider 등)로 작업하든 이 파일을 참조한다.
- `CLAUDE.md`는 호환성을 위해 유지되며, 이 파일을 가리키는 포인터로만 둔다. 다른 에이전트 특화 파일(`GEMINI.md`, `.cursorrules` 등)이 추가될 때도 같은 패턴으로 처리한다.
- 다음과 같은 변경이 발생하면 이 문서를 함께 갱신한다:
  - 새 도메인/모듈 추가, 패키지 구조 변경
  - 외부 API 추가/변경, 환경변수 추가/제거
  - 컨벤션·예외 처리·테스트 정책 변경
  - 캐시 전략·데이터 표시 정책 변경
  - 주요 PR 머지, 진행 중인 작업 흐름 변동

---

## 1. 프로젝트 목적

한 줄 정의: **FindEr는 지금 나를 받아줄 수 있는 가장 가까운 응급실을 지도에서 찾는 서비스다.**

왜 만드는가:

- 타겟 상황: 119를 부르지 않고 직접 이동하는 준응급 상황의 환자/보호자.
- 핵심 가치: 내 위치 주변 응급실을 거리, 병상 가용 상태, 갱신 시각, 증상별 수용 가능성 기준으로 빠르게 비교한다.
- 포트폴리오 목표: **신입 백엔드 개발자 포트폴리오**로서 외부 공공 API 연동, 캐싱, 장애 fallback, 단일 EC2 운영, 테스트, UX 완성도를 보여준다.
- 방향성: 단순 CRUD보다 "응급의료 데이터 수집 → 캐싱 → 가공 → API 제공 → 지도 UX"의 완성도를 우선한다.

---

## 2. 기술 스택

### Backend

| 항목 | 버전 | 근거 |
|---|---:|---|
| Java toolchain | 17 | `backend/build.gradle` |
| Spring Boot | 3.5.0 | `org.springframework.boot` Gradle plugin |
| Spring dependency management | 1.1.7 | Gradle plugin |
| Gradle wrapper | 8.14.4 | `backend/gradle/wrapper/gradle-wrapper.properties` |
| Spring Boot Starter Data JPA | Boot 3.5.0 managed | `backend/build.gradle` |
| Spring Boot Starter Validation | Boot 3.5.0 managed | `backend/build.gradle` |
| Spring Boot Starter Web | Boot 3.5.0 managed | `backend/build.gradle` |
| Lombok | Boot/Gradle managed | `backend/build.gradle` |
| MySQL driver | `com.mysql:mysql-connector-j` (Boot managed) | `backend/build.gradle` |
| Test | Spring Boot Starter Test, JUnit Platform | `backend/build.gradle` |

### Frontend

| 항목 | 버전 | 근거 |
|---|---:|---|
| React | ^19.2.5 | `frontend/package.json` |
| React DOM | ^19.2.5 | `frontend/package.json` |
| React Router DOM | ^7.14.2 | `frontend/package.json` |
| Vite | ^8.0.10 | `frontend/package.json` |
| `@vitejs/plugin-react` | ^6.0.1 | `frontend/package.json` |
| Axios | ^1.16.0 | `frontend/package.json` |
| ESLint | ^10.2.1 | `frontend/package.json` |

`qrcode.react`가 package.json에 남아 있으나, 응급카드 제거 이후 실 사용처가 없다 — 후속 정리 예정 (섹션 6 참고).

### Infra & 배포

| 항목 | 값 | 근거 |
|---|---|---|
| 배포 환경 | 단일 EC2 호스트에 직접 빌드/실행 | 운영 결정 |
| DB (운영) | EC2 host MySQL | `backend/src/main/resources/application.properties` |
| CI | GitHub Actions (main push → 자동 배포) | `.github/workflows/` |

**Docker, Kubernetes는 도입 안 함.** 단일 호스트 운영에서 컨테이너화/오케스트레이션의 이점이 크지 않다고 판단. 도입 필요성이 생기면 별도 설계/PR에서 처음부터 다룬다. 추측으로 Docker/K8s 설정·문서를 미리 추가하지 않는다.

### External APIs

| API | 용도 | 엔드포인트 |
|---|---|---|
| E-Gen 위치 조회 | 위치 기반 병원 후보 조회 | `getEgytLcinfoInqire` |
| E-Gen 기본정보 조회 | 병원 상세 fallback | `getEgytBassInfoInqire` |
| E-Gen 실시간 가용병상 | 응급실 `hpid` whitelist 출처 | `getEmrrmRltmUsefulSckbdInfoInqire` |
| E-Gen 차단 메시지 | 응급실/중증질환 차단 메시지 | `getEmrrmSrsillDissMsgInqire` |
| E-Gen 중증질환 수용가능정보 | 증상/시술 가능 코드 `mkioskty1..28` | `getSrsillDissAceptncPosblInfoInqire` |
| safetydata.go.kr | 전국 실시간 병상 스냅샷 | `DSSP-IF-00242` |
| Kakao Maps JS SDK | 프론트 지도 렌더링/장소 검색 | `dapi.kakao.com/v2/maps/sdk.js` |

E-Gen 기본 URL: `http://apis.data.go.kr/B552657/ErmctInfoInqireService`

### 환경변수

민감 정보는 환경변수로만 관리. 코드/문서에 직접 삽입 금지.

| 변수명 | 용도 |
|---|---|
| `DB_URL` | MySQL 연결 URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `EGEN_API_KEY` | E-Gen 공공데이터 API 키 |
| `BED_API_KEY` | safetydata.go.kr 실시간 병상정보 API 키 |
| `VITE_KAKAO_JS_KEY` | (프론트) Kakao Maps JS SDK 키 — client-visible 값 |
| `VITE_API_BASE_URL` | (프론트) 백엔드 API base URL |

---

## 3. 아키텍처 & 디렉토리 구조

### Backend package rule

기준 패키지: `com.finder`

```text
backend/src/main/java/com/finder
├── common/
│   ├── config/             # RestTemplate, CORS 등 공통 설정
│   ├── exception/          # 커스텀 예외
│   ├── response/           # 표준 에러 응답
│   └── GlobalExceptionHandler.java
└── hospital/
    ├── client/             # E-Gen, safetydata 외부 API client와 raw item
    ├── controller/         # REST API entrypoint
    ├── domain/             # HospitalInfo, BedSnapshot, HospitalStatus 등
    ├── dto/                # API Request/Response record
    └── service/            # use case, cache, scheduler
```

레이어 의존 방향:

```text
controller → service → repository/client/cache → domain
```

규칙:

- Controller가 Repository, 외부 API client, 캐시를 직접 호출하지 않는다.
- Service는 유스케이스를 조합한다. 병원 검색은 `HospitalService`가 기본정보 캐시, 병상 캐시, 차단 메시지, 중증질환 가능 코드를 결합한다.
- `client/`는 외부 API 호출과 raw 응답 parsing을 담당한다. 외부 API 장애는 빈 컬렉션 또는 Optional fallback으로 흡수하고 로그를 남긴다.
- `service/*Cache`는 스냅샷 캐시와 조회를 담당한다.
- `service/*Scheduler`는 외부 API를 주기적으로 갱신한다.
- 병원 정보는 DB에 저장하지 않는다. 기본정보·병상정보 모두 메모리 캐시.

### Frontend structure

```text
frontend/src
├── api/                # API 호출 wrapper. base URL 정규화 포함
├── components/         # 지도, 패널, 목록, 필터, 배지
├── constants/          # UI 도움말/상수
├── pages/              # route page
└── assets/
```

주요 역할:

- `pages/MapPage.jsx`: 지도 화면의 상위 상태 조합. 위치, 반경, 필터, 정렬, 상세 로딩, 지도 중심 재검색 상태를 관리한다.
- `components/KakaoMap.jsx`: Kakao Maps SDK 초기화, 마커/오버레이 렌더링, 지도 이동 이벤트 전달만 담당한다.
- `components/HospitalPanel.jsx`: 좌우 패널, 검색/필터/목록 UI 조합.
- `components/TreatmentFilter.jsx`: 증상 그룹과 `mkioskty` 코드 매핑. 선택된 모든 그룹을 만족해야 통과하고, 그룹 내부 코드는 OR로 본다.

---

## 4. 코딩 컨벤션

### Java

- DTO는 Java `record`를 기본으로 한다.
- Request/Response DTO를 명확히 분리한다.
- Entity는 setter를 만들지 않는다.
- Entity 생성자는 직접 노출하지 않고 정적 팩토리(`create`)를 사용한다.
- Entity에는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 둔다.
- 클래스와 public 메서드에는 한 줄 JavaDoc을 둔다.
- 자명한 주석은 쓰지 않는다. 비즈니스 규칙, 외부 API 제약, 장애 fallback 이유만 짧게 남긴다.
- 현재 영속 엔티티는 없다. 새 엔티티를 추가할 때 위 규칙을 따른다.

DTO 예시:

```java
public record HospitalSearchRequest(
        @NotNull Double lat,
        @NotNull Double lng,
        @Positive double radiusKm
) {}
```

JavaDoc 예시:

```java
/** 좌표/반경에 해당하는 병원 목록을 거리·가용 상태 기준으로 정렬해 반환한다. */
public HospitalListResponse getHospitals(double lat, double lng, double radiusKm) { ... }
```

예외 처리:

- 사용자-facing 예외는 커스텀 예외 + `@RestControllerAdvice` 전역 핸들러에서 처리한다.
- 표준 에러 응답은 `{ "code": "...", "message": "..." }` 형태다.
- 각 레이어에서 의미 없는 try-catch를 두지 않는다.
- 예외: 외부 API client, scheduler, 캐시는 장애 fallback을 위해 catch 후 로그를 남기고 빈 값/메모리 캐시로 계속 동작할 수 있다.

커스텀 예외 디렉토리:

```text
common/exception/
├── NotFoundException.java        # 리소스 없음 (404)
├── UnauthorizedException.java    # 인증 실패 (401)
└── InvalidRequestException.java  # 잘못된 요청 (400)
```

테스트:

- **domain**: 비즈니스 로직 단위 테스트 (JUnit5, 외부 의존 없음)
- **service**: 서비스 레이어 단위 테스트 (Mockito로 repository/client mock)
- **controller**: 슬라이스 테스트 (`@WebMvcTest`)
- 메서드명은 한글 행동 중심:

```java
@Test
void 반경_내_병원이_없으면_빈_목록을_반환한다() { ... }

@Test
void 갱신_시각이_30분_초과인_병상정보는_UNKNOWN으로_분류한다() { ... }
```

- given / when / then 구조로 작성한다.
- 구현 세부가 아니라 행동(behavior)을 검증한다.

### Frontend

- 지도 첫 화면이 실제 서비스 경험이어야 한다. 별도 랜딩 페이지를 우선 만들지 않는다.
- API base URL은 `frontend/src/api/*`에서 정규화한다.
- 클라이언트에서 쓰는 환경변수는 반드시 `VITE_` prefix를 사용한다.
- 지도 UI 상태는 다음을 분리한다.
  - 검색 기준 위치: `userLocation = customLocation ?? gpsLocation`
  - 현재 지도 중심: `viewportCenter`
  - 지도 중심 재검색 trigger: `refreshKey`
  - 같은 위치로 지도만 복귀시키는 trigger: `recenterKey`
- 증상별/병상별 정보는 사용자가 빠르게 비교할 수 있게 병상 수, 상태, 갱신 시각, 가능 시술 정보를 숨기지 않는다.

### Git

- GitHub Flow를 따른다.
- `main`: 항상 배포 가능 상태. push 시 자동 배포될 수 있다.
- 기능 브랜치: `feature/xxx`
- 버그 수정 브랜치: `fix/xxx`
- 커밋 메시지는 한글로 작성하고 타입 접두사(`feat`, `fix`, `refactor`, `test`, `docs`, `chore`)를 붙인다.
- 푸시한 커밋을 amend·force-push 하지 않는다. 로컬 amend는 푸시 전까지만.

브랜치 예시:

- `feature/hospital-search-api`
- `feature/map-center-marker`
- `fix/bed-cache-stale-threshold`

커밋 메시지 예시:

```text
feat: 지도 중심 재검색 기능 구현
fix: 토큰 중복 생성 버그 수정
refactor: 병원 서비스 레이어 분리
test: 병원 서비스 단위 테스트 추가
chore: GitHub Actions Node.js 버전 업데이트
```

---

## 5. 빌드/실행/테스트 명령어

### Backend

```bash
cd backend
./gradlew bootRun
./gradlew test
./gradlew build
./gradlew compileJava   # 빠른 컴파일 확인용
```

주의:

- `backend/settings.gradle`이 root project. 레포 루트에는 `settings.gradle`이 없다.
- Java target은 17. 로컬 Java 버전이 달라도 toolchain이 17로 고정된다.
- DB 환경변수 없이는 Spring Boot 실행이 실패할 수 있다. 컴파일·테스트는 가능.

### Frontend

```bash
cd frontend
npm install
npm run dev
npm run build
npm run lint
npm run preview
```

확인된 상태:

- `npm run build` 통과.
- `VITE_KAKAO_JS_KEY`가 없으면 Vite가 `%VITE_KAKAO_JS_KEY% is not defined` 경고를 출력하지만 build 자체는 완료된다.

---

## 6. 현재 진행 상황

작성 기준일: 2026-06-09

### 완료된 것

- **`hospital-detail-panel`** 시리즈(PR #18~21) 머지 완료. 병원 패널 분리, 정렬/필터 컨트롤, 인라인 상세, 스크롤 정리.
- **`feature/map-center-marker`** 머지 완료 (PR #25, merge commit `c98c6d5`).
  - 지도 중앙에 회색 핀 형태의 고정 기준점 표시.
  - `지도 중심에서 재검색` 버튼: 80m 이상 이탈 시 노출.
  - `현재 위치로` 버튼: 재조회 없이 지도 뷰만 검색 기준 위치로 복귀.
  - `recenterKey`로 KakaoMap 재중심 트리거 분리.

### 진행 중

- **응급카드 기능 제거** (`feature/remove-emergency-card`, PR #26):
  - 프론트 페이지(`CardCreatePage`/`CardViewPage`/`CardEditPage`)·`CardForm`·`api/card.js`·`api/cardStorage.js` 삭제.
  - `App.jsx` 라우트, `Header.jsx` 응급카드 버튼 제거.
  - 백엔드 `com.finder.card` 패키지 전체 삭제.
  - `common/config/AppConfig`의 `BCryptPasswordEncoder` bean, `build.gradle`의 `spring-security-crypto`, `application.properties`의 `app.base-url` 정리.
  - README/architecture/api-spec/db-schema/CLAUDE.md 문서 갱신.
  - 머지·배포 완료 후 EC2 MySQL에서 `DROP TABLE emergency_card;` 수동 실행 필요 (Hibernate `ddl-auto=update`라 자동 drop 안 됨).
- **본 PR (AGENTS.md 메인화)**: 이 문서를 메인 컨텍스트로 승격, `CLAUDE.md`는 포인터로 축소.

### 미해결/추적 항목

- **`qrcode.react`** 의존성: package.json에 남아 있으나 카드 제거 후 실 사용처 0건. 별도 PR로 정리.
- 백엔드 테스트가 거의 없다(`FindErApplicationTests`만 존재). 도메인 단위 테스트부터 채워야 한다.
- `application.properties`에 `jpa.hibernate.ddl-auto=update`. 영속 엔티티가 0개라 사실상 무영향이지만, 운영 DB에 잔존 테이블이 남을 수 있어 머지 후 수동 확인.

### 다음 목표

- 증상별 병원 정보 가시성 개선.
  - 참고 서비스: `https://my-doctor.io/map/baby119`
  - 목표: FindEr만의 특화 UX 정립. "왜 이 병원이 해당 증상에 적합한지"를 노출.
- 백엔드 도메인/서비스 단위 테스트 보강.
- E-Gen API 키 발급 전후 동작 안정성 점검.

---

## 7. 주의사항·금지사항

작업 안전:

- 사용자 변경사항을 임의로 revert하지 않는다.
- `git reset --hard`, `git checkout --`, 대량 삭제는 사용자 명시 요청 없이는 금지.
- `main` push는 자동 배포로 이어질 수 있으므로 기능 브랜치에서 작업한다.
- 커밋은 항상 새로 만든다. amend는 로컬에서만, 푸시 후에는 금지.

데이터/의료 UX:

- 병상 수와 갱신 시각은 의료 판단에 중요하므로 숨기지 않는다.
- `availableEmergencyBeds`가 음수이거나 갱신이 30분을 초과한 데이터는 `UNKNOWN` 처리한다.
- `UNKNOWN` 상태에서는 병상 수 숫자를 숨기고 "정보 없음"으로 표시한다.
- 오래된 데이터를 최신처럼 보이게 하지 않는다.
- 상태 카테고리: GREEN(여유, ≥4) / YELLOW(보통, 1~3) / RED(혼잡, 0) / UNKNOWN(정보 없음). 색상 마커와 배지에 사용.

외부 API:

- E-Gen 응답은 단건일 때 배열 대신 객체로 올 수 있다. parser는 배열/객체 모두 처리해야 한다.
- E-Gen 기본정보 API는 일반 의원/치과까지 대량 반환할 수 있다. 응급실 식별에는 실시간 가용병상 API의 `hpid` 목록을 whitelist로 활용한다.
- 한글 `STAGE1` query는 인코딩과 `build(true)` 이중 인코딩 방지 로직을 유지한다.
- 외부 API 키가 없을 때 앱이 최대한 기동되고 빈 데이터/UNKNOWN fallback으로 동작하게 한다.

Frontend:

- 지도 중앙 기준점은 회색 핀 형태를 유지한다. crosshair 스타일로 되돌리지 않는다.
- `현재 위치로` 버튼은 재조회 버튼이 아니다. 지도 중심만 검색 기준 위치로 복귀시킨다.
- `지도 중심에서 재검색`은 현재 지도 중심을 새 검색 기준으로 삼고 병원 목록을 다시 조회한다.
- Kakao Maps key는 client-visible 값이므로 `VITE_KAKAO_JS_KEY`로만 주입한다. 민감키를 프론트 코드에 하드코딩하지 않는다.

API:

- Base URL: `/api/v1`
- 성공 응답: HTTP 상태코드로 구분 (200, 201, 204)
- 에러 응답: 항상 `{ code, message }` 포맷

품질 기준:

- 성능 개선은 "측정 → 병목 확인 → 개선 → 재측정" 순서로 한다.
- 리팩터링은 기능 변경과 분리한다.
- 공통 추상화는 실제 중복이나 복잡도를 줄일 때만 추가한다.
- 테스트는 구현 세부가 아니라 행동을 검증한다.

---

## 8. 주요 설계 결정과 이유

### 병원 정보는 DB 저장보다 캐시 중심

- 병원 기본정보와 위치는 변동이 적어 메모리 캐시로 충분하다.
- 실시간 병상정보는 짧은 주기로 변하지만 모든 사용자에게 공통인 스냅샷이므로 메모리 캐시로 공유한다.
- 사용자 요청마다 외부 API를 직접 호출하면 응답 지연, 장애 전파, 호출량 증가 문제가 커진다.
- 단일 EC2 운영 동안은 메모리 캐시로 충분하다. 다중 인스턴스로 확장하면 분산 캐시·캐시 공유 전략을 그 시점에 별도로 설계한다.

### 30분 stale threshold

- 의료 데이터가 오래됐을 때 숫자를 그대로 노출하면 위험하다.
- 30분 초과 데이터는 `UNKNOWN`으로 산출해 UI에서 "정보 없음" 처리한다.

### E-Gen 실시간 응급실 API를 whitelist로 사용

- 기본정보 API는 응급실이 아닌 일반 의료기관까지 포함할 수 있다.
- 실시간 가용병상 API 응답은 응급실 운영 기관으로 한정되어 있어 `hpid` whitelist 출처로 더 적합하다.

### 지도 중심 재검색 UX

- 사용자는 지도를 움직인 뒤 "어디를 기준으로 재검색되는지" 알아야 한다.
- 지도 중심에 고정 기준점 마커를 둔다.
- 기존 현재 위치 마커와 같은 핀 계열 형태를 쓰되, 색은 옅은 회색으로 구분한다.
- 검색 후 중심이었던 지점을 보여주는 것이 아니라, 재검색 전부터 기준점을 보여주는 것이 핵심 요구다.

### `현재 위치로`와 `지도 중심에서 재검색` 분리

- 지도 복귀는 사용자가 길을 잃었을 때 뷰만 되돌리는 동작이다.
- 재검색은 지도 중심 좌표를 새 검색 기준으로 삼아 API를 다시 호출하는 동작이다.
- 두 동작을 분리해야 불필요한 외부 API 호출을 줄이고 UX도 명확해진다.

### 증상별 필터는 그룹 AND, 그룹 내부 OR

- 사용자가 여러 증상/상황을 선택하면 선택한 모든 그룹을 만족하는 병원을 찾는다.
- 하나의 그룹은 여러 `mkioskty` 코드로 표현될 수 있으므로 그룹 내부는 OR다.
- 다음 작업에서는 단순 필터를 넘어 "왜 이 병원이 해당 증상에 적합한지"를 더 잘 보여줘야 한다.

### 응급카드 기능 제거 (2026-06-09)

- 초기 기능으로 카드 CRUD/QR 공유가 구현되어 있었으나, 핵심 가치(가까운 가용 응급실 찾기)와 맞지 않아 제거 결정.
- 프론트 페이지·API·헤더 진입, 백엔드 `com.finder.card` 패키지, `BCryptPasswordEncoder` bean, `spring-security-crypto`, `app.base-url` 모두 정리.
- 운영 DB의 `emergency_card` 테이블은 PR #26 머지 후 수동으로 `DROP TABLE` 필요.

### Docker / Kubernetes 미도입

- 현재는 단일 EC2 호스트에 직접 빌드/실행. 컨테이너화/오케스트레이션의 이점이 크지 않다고 판단.
- 도입 필요성(다중 인스턴스, 환경 격리, 배포 안정성 강화)이 생기면 별도 설계/PR에서 처음부터 다룬다.
- 추측으로 Dockerfile, docker-compose, K8s manifest를 미리 추가하지 않는다.
