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
        service = new OpportunityCostService();
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
}
