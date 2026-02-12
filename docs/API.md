# API 문서

## 기회비용 계산 API

### POST /api/calculate

두 선택지의 기회비용을 계산하여 비교합니다.

#### 요청 (Request)

**Content-Type:** `application/json`

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

**필드 설명:**
- `hourlyWage` (Long, 필수): 시급 (원/시간), 최소값: 1
- `optionA` (Object, 필수): 선택지 A
  - `timeMinutes` (Integer, 필수): 소요 시간 (분), 최소값: 0
  - `directCost` (Long, 필수): 직접 비용 (원), 최소값: 0
- `optionB` (Object, 필수): 선택지 B
  - `timeMinutes` (Integer, 필수): 소요 시간 (분), 최소값: 0
  - `directCost` (Long, 필수): 직접 비용 (원), 최소값: 0

#### 응답 (Response)

**성공 (200 OK)**

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
  "formula": "총 비용 = 직접 비용 + (시급 ÷ 60) × 소요 시간(분)\n분당 가치 = 250원/분\n..."
}
```

**필드 설명:**
- `optionA` / `optionB`: 각 선택지의 비용 분해
  - `directCost`: 직접 비용 (원)
  - `timeCost`: 시간 비용 (원) = (시급 ÷ 60) × 소요 시간(분), floor 처리
  - `totalCost`: 총 비용 (원) = 직접 비용 + 시간 비용
- `recommendation`: 추천 선택지 ("A", "B", 또는 "동일")
- `costDifference`: 총 비용 차액 (원, 절댓값)
- `formula`: 계산식 설명 문자열

**오류 (400 Bad Request)**

입력 검증 오류 시:

```json
{
  "hourlyWage": "시급은 필수 입력 항목입니다.",
  "optionA.timeMinutes": "소요 시간은 0분 이상이어야 합니다."
}
```

**오류 (500 Internal Server Error)**

서버 오류 시:

```json
{
  "error": "서버 오류가 발생했습니다.",
  "message": "잠시 후 다시 시도해주세요."
}
```

#### 계산식

```
총 비용(TC) = 직접 비용(C) + (시급(W) ÷ 60) × 소요 시간(분)(T)

- W: 시급 (원/시간)
- T: 소요 시간 (분)
- C: 직접 비용 (원)
- 시간 비용은 floor 처리 (소수점 버림)
```

#### 예시

**예시 1: 더 싼 마트 vs 가까운 편의점**

```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "hourlyWage": 15000,
    "optionA": {
      "timeMinutes": 10,
      "directCost": 3000
    },
    "optionB": {
      "timeMinutes": 40,
      "directCost": 2300
    }
  }'
```

**예시 2: 직접 요리 vs 배달**

```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "hourlyWage": 20000,
    "optionA": {
      "timeMinutes": 60,
      "directCost": 6000
    },
    "optionB": {
      "timeMinutes": 10,
      "directCost": 14000
    }
  }'
```
