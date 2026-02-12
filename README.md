## Opportunity-cost-calculation

### 🎯 문제 정의
- 사람들은 **지출(원)**에는 민감하지만, **기회비용(시간)**은 과소평가하는 경향이 있습니다.
- 그 결과, **1,000원을 아끼기 위해 30분을 더 쓰는 선택**처럼 “시간의 가치 잠식”이 발생하고, 결정 후 막연한 후회가 남습니다.
- 선택지가 많아질수록 비교 기준이 흔들려 **의사결정 피로**가 쌓이며, 이미 쓴 시간/노력 때문에 비합리적 선택을 지속하는 **매몰 비용** 상황도 생깁니다.

➡️ 본 웹앱은 사용자의 시급을 기준으로 **총비용(직접비용 + 시간비용)**을 계산해 **객관적 지표(Total Cost Index)**로 비교하고, 결론과 함께 **근거(중간값/수식)**를 제공하여 의사결정의 확신을 높입니다.

---

### 🧩 선택 이유
- 기회비용은 개념은 쉬워도 실생활에 **숫자로 적용하기 어렵고 기준이 흔들리기 쉬움**
- 총비용 지표로 비교하면 판단이 빨라져 **의사결정 피로 감소**
- 계산 모델이 단순·투명해 **근거 제시/검증(테스트)**이 용이

---

### 🧰 기능 범위

#### ✅ 핵심 기능(MVP)
1) 시급 입력 → 분당 가치 자동 계산  
2) 선택지 A/B 입력: 소요시간(분), 직접비용(원)  
3) 결과: A 총비용, B 총비용, 추천 선택, 차액(원)  
4) 근거: 직접비용/시간비용 분해 + 계산식(토글)  
5) Total Cost Index(= 총비용)로 비교 결과를 직관적으로 표시  
6) 프리셋 예시 제공(온보딩)

#### ➕ 부가 기능(확장) ✅ 구현 완료
- ✅ 다안 비교(3~5개) - 동적 선택지 추가/제거 지원
- ✅ 결정 히스토리 저장(localStorage) - 최대 20개 저장, 재사용 가능
- ✅ 계산 결과 캐싱 - 성능 최적화
- ✅ 배포 준비 완료 - Docker, Heroku, Railway, AWS, GCP 지원
- ✅ 계산 로직 단위 테스트 - 서비스 및 컨트롤러 테스트

---

### 🧮 계산 로직(정의)

#### 📌 핵심 수식
사용자의 시급을 $W$, 소요 시간을 $T$(분), 직접 비용을 $C$라고 할 때 총 비용($TC$)은 다음과 같습니다.

$$TC = C + \left( \frac{W}{60} \times T \right)$$

- $W$ : 시간당 가치 (원/시간)  
- $T$ : 소요 시간 (분)  
- $C$ : 직접 비용 (원)  
- Total Cost Index = 기본적으로 $TC$ 값

#### 🔢 소수점 처리(원 단위)
- 최종 총비용은 **원 단위 절삭(floor)** 처리  
  (예: 5,500.9원 → 5,500원)

---

### 👥 대상 사용자
- 직장인/프리랜서/학생 등 “시간이 중요한 자원”인 사용자
- 절약/합리적 소비를 원하지만 시간까지 반영한 기준이 없어 자주 고민하는 사용자

---

### 🧭 사용자 시나리오

#### 시나리오 1) 더 싼 마트 vs 가까운 편의점
- 시급: 15,000원 → 분당 250원
- A: 3,000원 / 10분
- B: 2,300원 / 40분
- A 총비용 = 3,000 + 10×250 = 5,500원
- B 총비용 = 2,300 + 40×250 = 12,300원  
→ 결론: **A가 유리(시간비용이 판단을 바꿈)**

#### 시나리오 2) 직접 요리 vs 배달
- 시급: 20,000원 → 분당 333원
- A: 6,000원 / 60분
- B: 14,000원 / 10분  
→ 직감과 결론이 달라질 수 있어 **근거(시간비용/수식) 표시가 중요**

#### 시나리오 3) 무료배송 기다리기 vs 유료배송 빠르게 받기
- A: 0원 / 추가 대기 시간 발생
- B: 비용 발생 / 대기 시간 절감  
→ 사용자가 “추가로 낭비되는 시간(분)”을 입력하여 비교

---

### ⚠️ 엣지 케이스 및 처리

#### ✅ 기본 엣지 케이스
1) 시급 0/음수/문자 → 숫자만 허용, 1 이상 제한, 오류 메시지  
2) 시간 0분 → 허용(시간비용 0)  
3) 비용 0원 → 허용(무료 옵션)  
4) 총비용 동일 → “차이 없음(동일)” 표시  
5) 큰 값 입력 → 경고(실수 가능성 안내), 계산은 유지  

#### 🧷 추가 엣지 케이스(단위/표현/인사이트)
6) 단위 혼동(시간: 분 vs 시/분) → `1시간 20분 = 80분` 변환 지원(또는 입력 예시/placeholder 제공)  
7) 시급이 비현실적으로 높은 경우 → “시간의 가치가 매우 큼” 인사이트 메시지(단정 대신 가능성 표현)  
8) 소수점 처리 기준 명시 → 최종 총비용 **원 단위 절삭(floor)** 고정

---

## (2) 개선 과정 기록

개발 과정에서의 변경 사항과 문제 해결 흐름은 아래 문서에 정리합니다.

- [CHANGELOG](docs/CHANGELOG.md): 변경 요약 / 변경 이유 / 영향 / 증빙(커밋·PR)
- [TROUBLESHOOTING](docs/TROUBLESHOOTING.md): 막힘(증상) → 원인 가설 → 시도 → 해결 → 배운 점
- [RETROSPECTIVE](docs/RETROSPECTIVE.md): 우선순위 조정 및 시간 배분 회고(최종 요약)

---

## (3) AI 활용 기록

AI 사용 내역은 “무엇을 요청했고, 어떻게 검증/수정했는지” 중심으로 아래 문서에 누적 기록합니다.

- [AI_LOG](docs/AI_LOG.md): 컨텍스트 → AI 요청 → 결과 요약 → 검증 → 수정 → 반영(커밋·PR)

---

## (4) 실행 방법

### 사전 요구사항
- Java 17 이상
- Maven 3.6 이상

### 1) 프로젝트 빌드
```bash
mvn clean install
```

### 2) 애플리케이션 실행
```bash
mvn spring-boot:run
```

또는 빌드된 JAR 파일 실행:
```bash
java -jar target/opportunity-cost-calculation-1.0.0.jar
```

### 3) 접속
브라우저에서 다음 주소로 접속합니다:
- http://localhost:8080

### 4) 테스트 실행
```bash
mvn test
```

---

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.2.0
- **Build Tool**: Maven
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla JS)
- **Template Engine**: Thymeleaf (선택사항)
- **Validation**: Jakarta Validation
- **Testing**: JUnit 5

---

## 프로젝트 구조

```
opportunity-cost-calculation/
├── src/
│   ├── main/
│   │   ├── java/com/opportunitycost/
│   │   │   ├── OpportunityCostApplication.java    # 메인 애플리케이션
│   │   │   ├── controller/                        # REST API 컨트롤러
│   │   │   ├── service/                           # 비즈니스 로직
│   │   │   ├── model/                             # 도메인 모델
│   │   │   └── dto/                               # 데이터 전송 객체
│   │   └── resources/
│   │       ├── static/                            # 정적 리소스 (CSS, JS)
│   │       ├── templates/                         # HTML 템플릿
│   │       └── application.properties             # 설정 파일
│   └── test/
│       └── java/com/opportunitycost/              # 테스트 코드
├── pom.xml                                         # Maven 설정
└── README.md
```

---

## API 엔드포인트

### 1) POST `/api/calculate` — 2개 선택지 비교
기회비용을 계산합니다.

**Request Body:**
```json
{
  "hourlyWage": 15000,
  "optionA": {
    "timeMinutes": 10,
    "directCost": 3000
  },
  "optionB": {
    "timeMinutes": 40,
    "directCost": 2300
  }
}
```

**Response:**
```json
{
  "optionA": {
    "directCost": 3000,
    "timeCost": 2500,
    "totalCost": 5500
  },
  "optionB": {
    "directCost": 2300,
    "timeCost": 10000,
    "totalCost": 12300
  },
  "recommendation": "A",
  "costDifference": 6800,
  "formula": "총 비용 = 직접 비용 + (시급 ÷ 60) × 소요 시간(분)..."
}
```

### 2) POST `/api/calculate/multi` — 다안 비교 (3~5개 선택지)
여러 선택지(3~5개)의 총비용을 한 번에 계산·비교합니다.

**Request Body:**
```json
{
  "hourlyWage": 15000,
  "options": [
    { "timeMinutes": 10, "directCost": 3000 },
    { "timeMinutes": 40, "directCost": 2300 },
    { "timeMinutes": 15, "directCost": 5000 }
  ]
}
```

**Response (예시):**
```json
{
  "results": [
    {
      "optionNumber": 1,
      "optionName": "선택지 A",
      "breakdown": {
        "directCost": 3000,
        "timeCost": 2500,
        "totalCost": 5500
      }
    },
    {
      "optionNumber": 2,
      "optionName": "선택지 B",
      "breakdown": {
        "directCost": 2300,
        "timeCost": 10000,
        "totalCost": 12300
      }
    }
  ],
  "recommendedOption": 1,
  "minTotalCost": 5500,
  "maxTotalCost": 12300,
  "maxDifference": 6800,
  "formula": "총 비용 = 직접 비용 + (시급 ÷ 60) × 소요 시간(분)..."
}
```
