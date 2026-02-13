package com.opportunitycost.service;

import com.opportunitycost.dto.CalculationRequest;
import com.opportunitycost.dto.CalculationResponse;
import com.opportunitycost.dto.CostBreakdown;
import com.opportunitycost.model.ComparisonOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 기회비용 계산 서비스 테스트
 */
class OpportunityCostServiceTest {

    private OpportunityCostService service;

    @BeforeEach
    void setUp() {
        // 캐시 서비스는 인메모리 구현이므로 테스트에서 직접 생성하여 주입
        CalculationCacheService cacheService = new CalculationCacheService();
        service = new OpportunityCostService(cacheService);
    }

    @Test
    @DisplayName("기본 계산 테스트 - 선택지 A가 더 유리한 경우")
    void testCalculate_OptionAIsBetter() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(10, 3000L));
        request.setOptionB(new ComparisonOption(40, 2300L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("A", response.getRecommendation());
        
        // 선택지 A: 3,000 + (15,000/60 × 10) = 3,000 + 2,500 = 5,500원
        CostBreakdown optionA = response.getOptionA();
        assertEquals(3000L, optionA.getDirectCost());
        assertEquals(2500L, optionA.getTimeCost());
        assertEquals(5500L, optionA.getTotalCost());

        // 선택지 B: 2,300 + (15,000/60 × 40) = 2,300 + 10,000 = 12,300원
        CostBreakdown optionB = response.getOptionB();
        assertEquals(2300L, optionB.getDirectCost());
        assertEquals(10000L, optionB.getTimeCost());
        assertEquals(12300L, optionB.getTotalCost());

        // 차액: 12,300 - 5,500 = 6,800원
        assertEquals(6800L, response.getCostDifference());
    }

    @Test
    @DisplayName("선택지 B가 더 유리한 경우")
    void testCalculate_OptionBIsBetter() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(20000L);
        request.setOptionA(new ComparisonOption(60, 6000L));
        request.setOptionB(new ComparisonOption(10, 14000L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("B", response.getRecommendation());
        
        // 선택지 A: 6,000 + (20,000/60 × 60) = 6,000 + 20,000 = 26,000원
        CostBreakdown optionA = response.getOptionA();
        assertEquals(6000L, optionA.getDirectCost());
        assertEquals(20000L, optionA.getTimeCost());
        assertEquals(26000L, optionA.getTotalCost());

        // 선택지 B: 14,000 + (20,000/60 × 10) = 14,000 + 3,333 = 17,333원 (floor 처리)
        CostBreakdown optionB = response.getOptionB();
        assertEquals(14000L, optionB.getDirectCost());
        assertEquals(3333L, optionB.getTimeCost()); // 3333.33... → 3333 (floor)
        assertEquals(17333L, optionB.getTotalCost());

        // 차액: 26,000 - 17,333 = 8,667원
        assertEquals(8667L, response.getCostDifference());
    }

    @Test
    @DisplayName("총 비용이 동일한 경우")
    void testCalculate_SameTotalCost() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(10000L);
        // A: 1,000 + (10,000/60 × 6) = 1,000 + 1,000 = 2,000원
        // B: 2,000 + (10,000/60 × 0) = 2,000 + 0 = 2,000원
        request.setOptionA(new ComparisonOption(6, 1000L));
        request.setOptionB(new ComparisonOption(0, 2000L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("동일", response.getRecommendation());
        assertEquals(0L, response.getCostDifference());
    }

    @Test
    @DisplayName("시간이 0분인 경우 (시간 비용 없음)")
    void testCalculate_ZeroTime() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(0, 5000L));
        request.setOptionB(new ComparisonOption(30, 3000L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        CostBreakdown optionA = response.getOptionA();
        assertEquals(5000L, optionA.getDirectCost());
        assertEquals(0L, optionA.getTimeCost());
        assertEquals(5000L, optionA.getTotalCost());
    }

    @Test
    @DisplayName("직접 비용이 0원인 경우 (무료 옵션)")
    void testCalculate_ZeroDirectCost() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(120, 0L));
        request.setOptionB(new ComparisonOption(10, 3000L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        CostBreakdown optionA = response.getOptionA();
        assertEquals(0L, optionA.getDirectCost());
        assertEquals(30000L, optionA.getTimeCost()); // 15,000/60 × 120 = 30,000
        assertEquals(30000L, optionA.getTotalCost());
    }

    @Test
    @DisplayName("소수점 처리 테스트 (floor)")
    void testCalculate_FloorHandling() {
        // Given: 시급이 60으로 나누어떨어지지 않는 경우
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(10000L);
        // 분당 가치: 10,000/60 = 166.666...
        // 1분: 166.666... → floor → 166원
        request.setOptionA(new ComparisonOption(1, 0L));
        request.setOptionB(new ComparisonOption(0, 0L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        CostBreakdown optionA = response.getOptionA();
        assertEquals(166L, optionA.getTimeCost()); // floor 처리 확인
    }

    @Test
    @DisplayName("매우 큰 시급 입력 (경고 임계값 이상)")
    void testCalculate_VeryHighWage() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(1_500_000L); // 경고 임계값(1,000,000) 이상
        request.setOptionA(new ComparisonOption(10, 1000L));
        request.setOptionB(new ComparisonOption(20, 0L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then - 경고는 나지만 계산은 정상적으로 수행됨
        assertNotNull(response);
        // 시간 비용: 1,500,000/60 × 10 = 250,000원
        assertEquals(250000L, response.getOptionA().getTimeCost());
    }

    @Test
    @DisplayName("매우 긴 시간 입력")
    void testCalculate_VeryLongTime() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(500, 0L)); // 500분 = 8시간 이상
        request.setOptionB(new ComparisonOption(10, 0L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        assertEquals("B", response.getRecommendation());
        // A의 시간 비용: 15,000/60 × 500 = 125,000원
        assertEquals(125000L, response.getOptionA().getTimeCost());
    }

    @Test
    @DisplayName("캐시 동작 테스트")
    void testCalculate_CacheWorks() {
        // Given
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(10, 3000L));
        request.setOptionB(new ComparisonOption(40, 2300L));

        // When - 첫 번째 호출
        CalculationResponse response1 = service.calculate(request);
        
        // When - 두 번째 호출 (동일한 요청)
        CalculationResponse response2 = service.calculate(request);

        // Then - 결과가 동일해야 함 (캐시에서 반환)
        assertEquals(response1.getRecommendation(), response2.getRecommendation());
        assertEquals(response1.getCostDifference(), response2.getCostDifference());
    }

    @Test
    @DisplayName("시간 비용이 직접 비용의 100배를 초과하는 경우 (경고 발생)")
    void testCalculate_TimeCostExceedsDirectCost100Times() {
        // Given: 시간 비용이 직접 비용의 100배를 초과하는 케이스
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(100000L); // 시급 100,000원
        // A: 직접비용 1,000원, 소요시간 1,000분 → 시간비용 1,666,666원 (약 1,667배)
        request.setOptionA(new ComparisonOption(1000, 1000L));
        request.setOptionB(new ComparisonOption(10, 5000L));

        // When - 계산 실행 (경고 로그는 발생하지만 계산은 정상 진행)
        CalculationResponse response = service.calculate(request);

        // Then - 계산은 정상적으로 완료됨
        assertNotNull(response);
        CostBreakdown optionA = response.getOptionA();
        // 시간 비용: 100,000/60 × 1,000 = 1,666,666원
        assertEquals(1666666L, optionA.getTimeCost());
        // 총 비용: 1,000 + 1,666,666 = 1,667,666원
        assertEquals(1667666L, optionA.getTotalCost());
    }

    @Test
    @DisplayName("총 비용이 10억원을 초과하는 경우 (경고 발생)")
    void testCalculate_TotalCostExceeds1Billion() {
        // Given: 총 비용이 10억원을 초과하는 케이스
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(100000000L); // 시급 1억원 (상한선)
        // A: 직접비용 0원, 소요시간 1,000분 → 시간비용 1,666,666,666원 (약 16.7억)
        request.setOptionA(new ComparisonOption(1000, 0L));
        request.setOptionB(new ComparisonOption(10, 5000L));

        // When - 계산 실행 (경고 로그는 발생하지만 계산은 정상 진행)
        CalculationResponse response = service.calculate(request);

        // Then - 계산은 정상적으로 완료됨
        assertNotNull(response);
        CostBreakdown optionA = response.getOptionA();
        // 시간 비용: 100,000,000/60 × 1,000 = 1,666,666,666원
        assertEquals(1666666666L, optionA.getTimeCost());
        // 총 비용: 0 + 1,666,666,666 = 1,666,666,666원
        assertEquals(1666666666L, optionA.getTotalCost());
    }

    @Test
    @DisplayName("시급이 60원 미만인 경우 시간 비용 0원 경고")
    void testCalculate_VeryLowWageResultsInZeroTimeCost() {
        // Given: 시급이 매우 낮아서 시간 비용이 0원이 되는 케이스
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(30L); // 시급 30원 (60원 미만)
        request.setOptionA(new ComparisonOption(10, 1000L)); // 10분 소요
        request.setOptionB(new ComparisonOption(20, 800L));

        // When - 계산 실행 (경고 로그 발생)
        CalculationResponse response = service.calculate(request);

        // Then
        CostBreakdown optionA = response.getOptionA();
        // 시간 비용: 30/60 × 10 = 5원 → floor(5) = 5원
        assertEquals(5L, optionA.getTimeCost());
        assertEquals(1005L, optionA.getTotalCost());
    }

    @Test
    @DisplayName("소요 시간이 0분이 아닌데 시간 비용이 0원인 경우 경고")
    void testCalculate_NonZeroTimeButZeroTimeCost() {
        // Given: 시급이 극도로 낮아서 1분에 대한 시간 비용이 0원
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(1L); // 시급 1원 (최소값)
        request.setOptionA(new ComparisonOption(1, 1000L)); // 1분 소요
        request.setOptionB(new ComparisonOption(10, 800L));

        // When
        CalculationResponse response = service.calculate(request);

        // Then
        CostBreakdown optionA = response.getOptionA();
        // 시간 비용: 1/60 × 1 = 0.0166... → floor(0.0166) = 0원
        assertEquals(0L, optionA.getTimeCost());
        assertEquals(1000L, optionA.getTotalCost());
    }
}
