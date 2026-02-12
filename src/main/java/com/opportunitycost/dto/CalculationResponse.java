package com.opportunitycost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기회비용 계산 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResponse {
    /**
     * 선택지 A의 비용 분해
     */
    private CostBreakdown optionA;
    
    /**
     * 선택지 B의 비용 분해
     */
    private CostBreakdown optionB;
    
    /**
     * 추천 선택지 ("A", "B", 또는 "동일")
     */
    private String recommendation;
    
    /**
     * 총 비용 차액 (원, 절댓값)
     */
    private Long costDifference;
    
    /**
     * 계산식 설명
     */
    private String formula;
}
