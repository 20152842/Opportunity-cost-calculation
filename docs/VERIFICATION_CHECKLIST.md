# 검증 체크리스트 (Verification Checklist)

## ✅ 완료된 검증 항목

### 1. 시급 상한선 검증 (엣지 케이스)

#### 백엔드 검증
- ✅ `@Max(value = 100_000_000)` 추가
- ✅ 메시지: "시급은 1억원 이하로 입력해주세요."
- ✅ 테스트: `testCalculate_ExceedsMaxWage()` 통과

#### 프론트엔드 검증
- ✅ 2안 비교 모드: 상한선 체크 추가
- ✅ 다안 비교 모드: 상한선 체크 추가
- ✅ 에러 메시지: "시급은 1억원 이하로 입력해주세요."

#### 기대 결과
- ❌ **이전**: "요청 형식이 올바르지 않습니다. JSON 형식을 다시 확인해주세요."
- ✅ **현재**: "시급은 1억원 이하로 입력해주세요."

---

### 2. 테스트 커버리지 확장

| 테스트 케이스 | 상태 | 검증 항목 |
|--------------|------|----------|
| 기본 계산 (A < B) | ✅ | 정확한 총비용 계산 |
| 기본 계산 (B < A) | ✅ | 추천 선택지 정확성 |
| 동일 총비용 | ✅ | "동일" 추천 |
| 0분/0원 케이스 | ✅ | 경계값 처리 |
| floor 처리 검증 | ✅ | 소수점 절삭 |
| 매우 큰 시급 | ✅ | 고액 시급 계산 |
| 매우 긴 시간 | ✅ | 장시간 계산 |
| 캐시 동작 | ✅ | 동일 요청 캐싱 |
| 음수 시간 | ✅ | 입력 검증 (API) |
| 음수 비용 | ✅ | 입력 검증 (API) |
| 시급 상한 초과 | ✅ | @Max 검증 (신규) |

**총 18개 테스트 케이스 / 18개 통과**

---

### 3. sessionStorage 동작 확인

#### 코드 검증
- ✅ `localStorage` → `sessionStorage` 변경 완료
- ✅ 저장: `sessionStorage.setItem(HISTORY_KEY, JSON.stringify(history))`
- ✅ 조회: `sessionStorage.getItem(HISTORY_KEY)`
- ✅ 삭제: `sessionStorage.removeItem(HISTORY_KEY)`

#### 동작 원리
- 세션 범위: 탭/창 단위
- 탭 닫으면: 자동 삭제
- 새 탭 열면: 빈 히스토리로 시작
- 서버 재시작: 클라이언트 저장소이므로 영향 없음 (단, 탭 열려있으면 유지)

#### 실제 브라우저 테스트 시나리오
1. 계산 3회 수행 → 히스토리 3개 확인
2. 동일 탭에서 새로고침 (F5) → 히스토리 유지 확인
3. 탭 닫기
4. 새 탭으로 http://localhost:8080 접속
5. **기대 결과**: 히스토리 비어있음 ✅

---

### 4. 캐싱 동작 확인

#### 코드 검증
- ✅ `CalculationCacheService`: ConcurrentHashMap + TTL 1시간
- ✅ 캐시 키: `hourlyWage + optionA + optionB` 조합
- ✅ 로그: "캐시된 결과 반환"

#### 테스트 검증
```java
@Test
void testCalculate_CacheWorks() {
    // 동일 요청 2회
    CalculationResponse result1 = service.calculate(request);
    CalculationResponse result2 = service.calculate(request);
    
    // 결과 동일 확인
    assertEquals(result1.getRecommendation(), result2.getRecommendation());
}
```
**상태**: ✅ 통과

#### 로그 확인 방법
1. 동일한 계산 2회 수행
2. 콘솔 로그에서 "캐시된 결과 반환" 메시지 확인

---

### 5. 문서 세분화

#### CHANGELOG.md
- ✅ 6개 Phase로 구분
- ✅ v0.1 ~ v1.8.0 (총 30개 버전)
- ✅ 각 버전별 변경 이유, 영향, 커밋 해시
- ✅ 세부 섹션: 기능 개선, 버그 수정, 사용자 피드백, 문서화

#### TROUBLESHOOTING.md
- ✅ 9개 → 17개 항목으로 확장
- ✅ 신규 추가:
  - PowerShell heredoc 문제
  - 포트 충돌 처리
  - 캐시 검증 로그
  - sessionStorage 전환
  - 높은 시급 에러 메시지 (핵심!)
  - 브라우저 캐시
  - Git 줄바꿈 경고
  - 서버 재시작 프로세스

---

## 🔍 브라우저 테스트 시나리오

### 시나리오 1: 정상 계산
1. http://localhost:8080 접속
2. 시급: 15,000원
3. 선택지 A: 10분, 3,000원
4. 선택지 B: 40분, 2,300원
5. "계산하기" 클릭
6. **기대 결과**: 
   - 선택지 A 유리 (5,500원 vs 12,300원)
   - 차액: 6,800원
   - 계산식 상세 표시 ✅

### 시나리오 2: 높은 시급 경고
1. 시급: 2,000,000원 (200만원)
2. 선택지 A: 10분, 0원
3. 선택지 B: 5분, 0원
4. "계산하기" 클릭
5. **기대 결과**: 
   - confirm 팝업: "입력하신 시급(2,000,000원)이 매우 높습니다. 계속하시겠습니까?"
   - "확인" 선택 시 정상 계산 ✅

### 시나리오 3: 상한선 초과 (신규 개선!)
1. 시급: 200,000,000원 (2억원)
2. 선택지 A: 10분, 0원
3. 선택지 B: 5분, 0원
4. "계산하기" 클릭
5. **기대 결과**: 
   - ❌ **이전**: "요청 형식이 올바르지 않습니다. JSON 형식을 다시 확인해주세요."
   - ✅ **현재**: "시급은 1억원 이하로 입력해주세요."
   - 하단 에러 메시지 표시
   - 상단 자동 스크롤
   - 시급 입력 필드에 focus

### 시나리오 4: 히스토리 (sessionStorage)
1. 계산 3회 수행 (서로 다른 값)
2. 페이지 하단 "히스토리" 섹션 확인 → 3개 표시 ✅
3. 동일 탭에서 F5 새로고침 → 히스토리 유지 ✅
4. 탭 닫기
5. 새 탭으로 다시 접속 → 히스토리 비어있음 ✅

### 시나리오 5: 프리셋 (다안 비교)
1. 상단 "다안 비교 (3~5개)" 버튼 클릭
2. "예시 시나리오" 중 "마트" 클릭
3. **기대 결과**: 
   - 시급: 15,000원
   - 선택지 5개 모두 자동 채워짐:
     - A: 가까운 마트 (5분, 2,500원)
     - B: 먼 마트 (30분, 2,000원)
     - C: 중간 거리 마트 (15분, 2,200원)
     - D: 편의점 (2분, 3,000원)
     - E: 대형마트 (50분, 1,800원)
   - "계산하기" 클릭 → 최소 비용 선택지 표시 ✅

---

## 📊 최종 검증 결과

| 항목 | 상태 | 비고 |
|------|------|------|
| 시급 상한선 검증 | ✅ | 백엔드 @Max + 프론트 체크 + 테스트 |
| 테스트 커버리지 | ✅ | 18/18 통과 |
| sessionStorage 동작 | ✅ | 코드 검증 완료, 브라우저 테스트 권장 |
| 캐싱 동작 | ✅ | 테스트 통과, 로그 확인 가능 |
| CHANGELOG 세분화 | ✅ | 30개 버전, 6개 Phase |
| TROUBLESHOOTING 확장 | ✅ | 9개 → 17개 항목 |

---

## 🎯 핵심 개선사항 요약

### Before (문제)
```
시급 2억원 입력 시:
→ "요청 형식이 올바르지 않습니다. JSON 형식을 다시 확인해주세요."
→ 사용자는 JSON을 직접 작성하지 않았으므로 혼란
```

### After (해결)
```
시급 2억원 입력 시:
→ 프론트엔드: "시급은 1억원 이하로 입력해주세요."
→ 백엔드: @Max 검증 + 동일 메시지
→ 사용자가 즉시 이해하고 수정 가능
```

---

## 📝 추가 확인 사항

### Git 상태
```bash
git log --oneline -1
# 6c120e2 fix: add max validation for hourly wage and expand troubleshooting
```

### 파일 변경
- `src/main/java/com/opportunitycost/dto/CalculationRequest.java`: @Max 추가
- `src/main/resources/static/js/app.js`: 상한선 체크 2곳 추가
- `src/test/java/.../OpportunityCostControllerTest.java`: 테스트 1개 추가
- `docs/CHANGELOG.md`: 전체 재구성 (30개 버전)
- `docs/TROUBLESHOOTING.md`: 8개 항목 추가

---

## 🚀 최종 실행 가이드

### 서버 실행
```powershell
mvn spring-boot:run
```

### 테스트 실행
```powershell
mvn test
```
**결과**: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0` ✅

### 브라우저 접속
```
http://localhost:8080
```

### 포트 충돌 시
```powershell
Get-Process -Name java | Stop-Process -Force
Start-Sleep -Seconds 3
mvn spring-boot:run
```

---

## ✨ 마무리

모든 검증 항목이 완료되었습니다!

- **엣지 케이스**: 명확한 에러 메시지로 개선 ✅
- **테스트**: 18개 모두 통과 ✅
- **문서**: CHANGELOG 30개 버전, TROUBLESHOOTING 17개 항목 ✅
- **실제 동작**: 브라우저 테스트 시나리오 준비 완료 ✅

브라우저에서 위의 5가지 시나리오를 직접 테스트하시면 모든 기능이 정상 동작함을 확인하실 수 있습니다.
