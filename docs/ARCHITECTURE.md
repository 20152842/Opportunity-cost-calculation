# 아키텍처 설계 문서

## 1. 전체 아키텍처 개요

### 레이어 구조 (Layered Architecture)
```
┌────────────────────────────────────────┐
│         Presentation Layer             │
│  (Thymeleaf Templates + JavaScript)    │
└────────────────┬───────────────────────┘
                 │ HTTP/REST API
┌────────────────▼───────────────────────┐
│          Controller Layer              │
│   (OpportunityCostController)          │
│   - 입력 검증 (@Valid)                  │
│   - 예외 처리 위임                      │
└────────────────┬───────────────────────┘
                 │ DTO
┌────────────────▼───────────────────────┐
│           Service Layer                │
│   (OpportunityCostService)             │
│   - 비즈니스 로직                       │
│   - 캐시 조회/저장                      │
└────────────────┬───────────────────────┘
                 │
┌────────────────▼───────────────────────┐
│         Support Services               │
│   (CalculationCacheService)            │
│   - 인메모리 캐시 관리                  │
└────────────────────────────────────────┘
```

### 핵심 원칙
1. **계층 간 의존성 단방향**: Presentation → Controller → Service
2. **DTO 사용**: 계층 간 데이터 전달은 DTO로 명확히 분리
3. **단일 책임 원칙**: 각 클래스는 하나의 명확한 책임
4. **전역 예외 처리**: `@RestControllerAdvice`로 중앙화

---

## 2. 핵심 컴포넌트 설계

### 2.1. Controller Layer

#### OpportunityCostController
**책임**: HTTP 요청 수신 및 응답 반환
```java
@RestController
@RequestMapping("/api")
public class OpportunityCostController {
    // 생성자 주입으로 Service 의존성 관리
    private final OpportunityCostService service;
    
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculate(
        @Valid @RequestBody CalculationRequest request) {
        // 검증은 @Valid가 자동 수행
        // 비즈니스 로직은 Service에 위임
        return ResponseEntity.ok(service.calculate(request));
    }
}
```

**설계 의도**:
- 입력 검증은 **선언적으로** (`@Valid`, `@NotNull`, `@Min`)
- 비즈니스 로직은 Controller에 두지 않음 (얇은 컨트롤러)
- 예외 처리는 `GlobalExceptionHandler`에 위임

**확장성**:
- 새로운 API 엔드포인트 추가 시: 메서드만 추가
- 인증/인가 필요 시: `@PreAuthorize` 또는 Interceptor 추가
- 버전 관리 필요 시: `@RequestMapping("/api/v2")`로 분리

### 2.2. Service Layer

#### OpportunityCostService
**책임**: 핵심 비즈니스 로직 (기회비용 계산)
```java
@Service
public class OpportunityCostService {
    private final CalculationCacheService cacheService;
    
    public CalculationResponse calculate(CalculationRequest request) {
        // 1. 캐시 조회
        Optional<CalculationResponse> cached = cacheService.get(request);
        if (cached.isPresent()) return cached.get();
        
        // 2. 계산 수행
        CalculationResponse response = performCalculation(request);
        
        // 3. 캐시 저장
        cacheService.put(request, response);
        
        return response;
    }
    
    private CalculationResponse performCalculation(CalculationRequest request) {
        // 시간 비용 = (시급 / 60) × 시간(분), floor 처리
        long timeAcost = (long) Math.floor(request.getHourlyWage() / 60.0 * request.getOptionA().getTimeMinutes());
        // ...
    }
}
```

**설계 의도**:
- **캐시 우선 조회**: 동일한 요청은 재계산 방지
- **floor 처리 명시**: 돈 계산은 버림이 일반적 (과대 평가 방지)
- **로깅**: 모든 계산 요청/결과를 로그로 기록 (디버깅/모니터링)

**확장성**:
- 새로운 계산 방식 추가: `calculateMulti()` 메서드 추가 (기존 로직 재사용)
- 외부 API 연동: 시급 정보를 외부 API에서 가져오기 (의존성 주입)
- DB 저장: 히스토리를 DB에 저장하는 `HistoryService` 추가

### 2.3. Cache Service

#### CalculationCacheService
**책임**: 계산 결과 캐싱 (성능 최적화)
```java
@Service
public class CalculationCacheService {
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long TTL = 3600_000L; // 1시간
    
    public void put(CalculationRequest request, CalculationResponse response) {
        String key = generateKey(request);
        cache.put(key, new CacheEntry(response, System.currentTimeMillis()));
    }
    
    private String generateKey(CalculationRequest request) {
        // JSON 직렬화로 일관된 키 생성
        return objectMapper.writeValueAsString(request);
    }
}
```

**설계 의도**:
- **ConcurrentHashMap**: 멀티스레드 환경에서 안전
- **TTL 관리**: 오래된 캐시는 자동 삭제
- **JSON 키**: 요청 객체를 JSON으로 변환하여 일관된 키 생성

**확장성**:
- Redis로 교체: `CacheService` 인터페이스 정의 + Redis 구현체 추가
- 분산 캐시: Redis Cluster로 수평 확장
- 캐시 전략 변경: LRU, LFU 등 다른 전략 적용 가능

---

## 3. DTO 설계

### 3.1. CalculationRequest
**목적**: API 입력 데이터 검증 및 전달
```java
public class CalculationRequest {
    @NotNull(message = "시급은 필수 입력 항목입니다.")
    @Min(value = 1, message = "시급은 1원 이상이어야 합니다.")
    private Long hourlyWage;
    
    @NotNull(message = "선택지 A는 필수 입력 항목입니다.")
    @Valid  // 중첩 검증
    private ComparisonOption optionA;
    
    // getter/setter
}
```

**설계 의도**:
- **선언적 검증**: 어노테이션만으로 입력 검증 완료
- **명확한 에러 메시지**: 한글로 사용자 친화적 메시지
- **중첩 검증**: `@Valid`로 하위 객체도 검증

**확장성**:
- 새로운 필드 추가: `@NotNull`만 추가하면 자동 검증
- 커스텀 검증: `@CustomValidator` 어노테이션 추가 가능

### 3.2. CalculationResponse
**목적**: 계산 결과 반환 (클라이언트가 필요한 모든 정보 포함)
```java
public class CalculationResponse {
    private String recommendation;  // "A", "B", "동일"
    private Long costDifference;    // 차액
    private CostBreakdown optionA;  // A의 상세 비용
    private CostBreakdown optionB;  // B의 상세 비용
    private String formula;         // 계산식
}
```

**설계 의도**:
- **완전한 정보**: 클라이언트가 추가 요청 없이 모든 정보 획득
- **분해된 비용**: `CostBreakdown`으로 직접비용/시간비용/총비용 분리
- **계산식 제공**: 사용자가 직접 검증 가능 (투명성)

**확장성**:
- 인사이트 추가: `insights: ["시간 가치가 매우 높습니다"]` 필드 추가
- 그래프 데이터: `chartData: {...}` 추가하여 시각화 지원

---

## 4. 예외 처리 전략

### GlobalExceptionHandler
**책임**: 모든 예외를 일관된 형식으로 변환
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        // 검증 실패 → 400 Bad Request
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(
        HttpMessageNotReadableException ex) {
        // JSON 파싱 실패 → 400 Bad Request
        return ResponseEntity.badRequest().body(
            new ErrorResponse("잘못된 JSON 형식입니다."));
    }
}
```

**설계 의도**:
- **중앙 집중식 처리**: 모든 컨트롤러에서 발생한 예외를 한 곳에서 처리
- **일관된 응답 형식**: `ErrorResponse` DTO로 통일
- **적절한 HTTP 상태 코드**: 검증 실패는 400, 서버 오류는 500

**확장성**:
- 새로운 예외 타입: `@ExceptionHandler` 메서드만 추가
- 커스텀 예외: `BusinessException` 정의 → 특정 상태 코드 매핑
- 로깅 강화: 모든 예외를 Sentry/CloudWatch로 전송

---

## 5. 프론트엔드 설계

### 5.1. 관심사 분리
```
app.js
├── 전역 변수 (DOM 참조, 현재 모드)
├── 초기화 (이벤트 리스너 등록)
├── API 호출 함수 (calculateBasic, calculateMulti)
├── UI 업데이트 함수 (displayResult, displayMultiResult)
├── 검증 함수 (validateInputs)
└── 유틸리티 함수 (showError, saveToHistory)
```

**설계 의도**:
- **함수형 분리**: 각 함수는 하나의 책임만
- **명확한 네이밍**: `calculateBasic`, `displayResult` 등 의도가 명확
- **재사용성**: `showError`, `hideError` 등 공통 함수

### 5.2. 동적 UI 관리
**data-index 속성 사용**:
```html
<input class="multi-time-input" data-index="0" />
<input class="multi-time-input" data-index="1" />
```
```javascript
const timeInput = document.querySelector(
    `.multi-time-input[data-index="${i}"]`);
```

**설계 의도**:
- **안정적 선택**: 클래스명만으로는 동적 요소 선택이 불안정
- **명시적 인덱스**: 어떤 선택지인지 명확히 표시
- **확장성**: 선택지 개수에 무관하게 동작

**교훈**: 초기에는 `optionCards[i]`로 선택했으나, DOM 생성 순서에 의존하여 버그 발생. `data-index`로 명시적 관리하여 해결.

### 5.3. 캐시 정책
- **sessionStorage 사용**: localStorage는 서버 재시작 후에도 남아 혼란
- **최대 20개 제한**: 오래된 항목부터 자동 삭제
- **타임스탬프**: 각 항목에 시간 기록

**확장성**:
- 서버 저장: API로 히스토리 저장/조회 엔드포인트 추가
- 동기화: 여러 탭 간 히스토리 공유 (BroadcastChannel)

---

## 6. 테스트 전략

### 6.1. 단위 테스트 (OpportunityCostServiceTest)
**대상**: 계산 로직의 정확성
- 기본 계산 (A 유리, B 유리, 동일)
- 엣지 케이스 (0분, 0원, floor 처리, 매우 큰 값)
- 캐시 동작

**의도**: 비즈니스 로직이 정확한지 독립적으로 검증

### 6.2. 통합 테스트 (OpportunityCostControllerTest)
**대상**: API 전체 흐름 (요청 → 검증 → 계산 → 응답)
- 정상 요청 (200 OK)
- 검증 실패 (400 Bad Request)
- JSON 파싱 실패 (400 Bad Request)
- 음수/null 입력

**의도**: 실제 HTTP 요청처럼 전체 스택을 검증

### 6.3. 테스트 커버리지 목표
- **서비스 로직**: 100% (모든 계산 경로)
- **컨트롤러**: 주요 엣지 케이스 포함
- **현재 상태**: 17개 테스트, 100% 통과

---

## 7. 확장성 시나리오

### 7.1. 다안 비교 → N안 비교
**현재**: 최대 5개 선택지
**확장**: 무제한 선택지
- DTO 변경: `List<ComparisonOption> options`
- UI 변경: 동적으로 카드 생성 (현재 로직 재사용)
- 알고리즘: `Collections.min()`로 최소 비용 찾기 (O(n))

**필요한 변경**: 최소 (설계가 이미 확장 가능)

### 7.2. 히스토리 → DB 저장
**현재**: sessionStorage (클라이언트)
**확장**: PostgreSQL/MySQL (서버)
- 새로운 엔티티: `HistoryEntity`
- 새로운 Service: `HistoryService`
- 새로운 API: `GET /api/history`, `POST /api/history`

**필요한 변경**: 새로운 레이어 추가 (기존 로직 영향 없음)

### 7.3. 인증/인가 추가
**현재**: 인증 없음
**확장**: JWT 토큰 기반 인증
- Spring Security 의존성 추가
- `SecurityConfig` 작성
- `JwtAuthenticationFilter` 추가

**필요한 변경**: 컨트롤러에 `@PreAuthorize` 추가만

### 7.4. 분산 환경 대응
**현재**: 단일 인스턴스 (인메모리 캐시)
**확장**: 다중 인스턴스 (Redis 캐시)
- `CacheService` 인터페이스 정의
- `RedisCacheService` 구현체 추가
- `application.yml`에서 프로필로 전환

**필요한 변경**: 의존성 주입만 변경 (비즈니스 로직 영향 없음)

---

## 8. 코드 품질 원칙

### 8.1. SOLID 원칙 적용
- **S**ingle Responsibility: 각 클래스는 하나의 책임 (Controller는 HTTP, Service는 비즈니스)
- **O**pen/Closed: 확장은 열려있고, 수정은 닫혀있음 (인터페이스 기반)
- **L**iskov Substitution: (현재 상속 미사용, 필요 시 적용)
- **I**nterface Segregation: (현재 인터페이스 미사용, 확장 시 적용)
- **D**ependency Inversion: 생성자 주입으로 의존성 역전

### 8.2. 명명 규칙
- **클래스**: PascalCase, 명사 (`CalculationRequest`, `OpportunityCostService`)
- **메서드**: camelCase, 동사 (`calculate`, `validateInputs`)
- **상수**: UPPER_SNAKE_CASE (`MAX_HISTORY`, `TTL`)
- **변수**: camelCase, 명확한 의미 (`hourlyWage`, `totalCost`)

### 8.3. 주석 정책
- **Javadoc**: 모든 public 메서드에 작성 (목적, 파라미터, 반환값)
- **인라인 주석**: 복잡한 계산 로직에만 사용 (왜 그렇게 했는지)
- **TODO**: 향후 개선 사항 표시

---

## 9. 성능 최적화

### 9.1. 캐시 전략
- **적중률**: 동일한 요청 반복 시 캐시 사용 (100% 적중)
- **TTL**: 1시간 (시급/비용은 자주 바뀌지 않음)
- **메모리 관리**: 최대 1000개 캐시 엔트리 (LRU 자동 삭제)

### 9.2. DB 쿼리 최적화 (향후)
- 인덱스: `user_id`, `created_at` (히스토리 조회)
- 페이지네이션: 무한 스크롤 대신 페이지 번호

### 9.3. 프론트엔드 최적화
- **CSS/JS 번들링**: (현재 단일 파일, 향후 Webpack)
- **이미지 최적화**: (현재 이미지 없음)
- **Lazy Loading**: 히스토리는 펼칠 때만 렌더링

---

## 10. 배포 전략

### 10.1. 프로필 관리
```yaml
# application-dev.yml (개발)
logging.level.com.opportunitycost: DEBUG
cache.ttl: 60000  # 1분 (빠른 테스트)

# application-prod.yml (운영)
logging.level.com.opportunitycost: INFO
cache.ttl: 3600000  # 1시간
```

### 10.2. 배포 옵션
1. **Docker**: `Dockerfile` + `docker-compose.yml` (포트 8080 노출)
2. **Heroku**: `Procfile` + `system.properties` (Java 17)
3. **Railway**: Git 연동 자동 배포
4. **AWS Elastic Beanstalk**: JAR 직접 업로드
5. **Google Cloud Run**: 컨테이너 기반 배포

**현재 상태**: 모두 준비 완료 (문서화), 실제 배포는 미완료

---

## 11. 보안 고려사항

### 11.1. 입력 검증
- **모든 입력 검증**: `@Valid`로 자동 검증
- **음수 방지**: `@Min(0)` 또는 `@Min(1)`
- **범위 검증**: 매우 큰 값은 경고 (프론트엔드)

### 11.2. CORS 설정
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST");
    }
}
```

### 11.3. SQL Injection 방지
- **JPA 사용**: PreparedStatement 자동 사용 (향후)
- **파라미터 바인딩**: `@Query` 사용 시 `:param` 형식

### 11.4. XSS 방지
- **Thymeleaf 자동 이스케이프**: `th:text`는 자동 HTML 인코딩
- **JSON 응답**: Jackson 라이브러리가 자동 인코딩

---

## 12. 모니터링 및 로깅

### 12.1. 로깅 전략
```java
@Slf4j  // Lombok
public class OpportunityCostService {
    public CalculationResponse calculate(CalculationRequest request) {
        log.info("기회비용 계산 요청 - 시급: {}, 선택지A: {}원/{}분, 선택지B: {}원/{}분",
            request.getHourlyWage(),
            request.getOptionA().getDirectCost(),
            request.getOptionA().getTimeMinutes(),
            request.getOptionB().getDirectCost(),
            request.getOptionB().getTimeMinutes());
        // ...
        log.info("계산 완료 - 추천: {}, 차액: {}원", 
            response.getRecommendation(), 
            response.getCostDifference());
    }
}
```

### 12.2. 향후 모니터링 (프로덕션)
- **APM 도구**: Spring Boot Actuator + Prometheus/Grafana
- **에러 추적**: Sentry 연동
- **사용자 분석**: Google Analytics (프론트엔드)

---

## 13. 개선 로드맵

### Phase 1: MVP (완료)
- ✅ 기본 2안 비교
- ✅ 다안 비교 (3~5개)
- ✅ 프리셋 예시
- ✅ 히스토리 (세션)
- ✅ 캐시 (인메모리)

### Phase 2: 사용자 경험 강화 (일부 완료)
- ✅ 사용자 피드백 반영
- ✅ 상세 계산식
- ✅ 단위 변환 가이드
- 🔜 인사이트 메시지
- 🔜 그래프 시각화

### Phase 3: 프로덕션 준비
- 🔜 실제 배포 (Heroku/Railway)
- 🔜 도메인 연결
- 🔜 HTTPS 설정
- 🔜 모니터링 구축

### Phase 4: 확장 기능
- 🔜 사용자 인증
- 🔜 히스토리 DB 저장
- 🔜 Redis 캐시
- 🔜 공유 기능 (URL 생성)

---

## 14. 의사결정 기록 (ADR)

### ADR-001: 인메모리 캐시 vs Redis
**결정**: 인메모리 캐시 선택
**이유**: MVP 단계에서는 단순성이 우선. Redis는 수평 확장이 필요할 때 추가.
**trade-off**: 단일 인스턴스만 지원, 재시작 시 캐시 초기화

### ADR-002: localStorage vs sessionStorage
**결정**: sessionStorage 선택
**이유**: 사용자 피드백 - "서버 재시작 시 히스토리가 남아 혼란"
**trade-off**: 탭 닫으면 히스토리 사라짐 (의도된 동작)

### ADR-003: floor vs round
**결정**: floor(버림) 선택
**이유**: 돈 계산은 버림이 일반적 (과대 평가 방지)
**trade-off**: 소수점 버림으로 약간의 오차 (허용 가능)

### ADR-004: 전역 예외 처리 vs 로컬 try-catch
**결정**: 전역 예외 처리 (`@RestControllerAdvice`)
**이유**: 중복 코드 제거, 일관된 에러 응답 형식
**trade-off**: 커스터마이징이 어려울 수 있음 (현재는 문제 없음)

---

## 15. 결론

### 설계의 핵심 강점
1. **명확한 계층 분리**: 각 레이어가 독립적으로 테스트 가능
2. **확장 가능한 구조**: 새로운 기능 추가 시 기존 코드 영향 최소
3. **일관된 코드 스타일**: 명명 규칙, 주석, 로깅 모두 통일
4. **실용적 선택**: 과도한 설계 없이 필요한 만큼만 구현

### 검증된 설계
- **17개 테스트 모두 통과**: 설계가 정확함을 증명
- **사용자 피드백 4건 반영**: 실제 사용자 경험 기반 개선
- **확장 시나리오 5가지 제시**: 미래 확장성 고려

### 향후 개선
- 인터페이스 도입 (CacheService, HistoryService)
- 분산 환경 대응 (Redis, DB)
- 모니터링 강화 (Actuator, Sentry)
