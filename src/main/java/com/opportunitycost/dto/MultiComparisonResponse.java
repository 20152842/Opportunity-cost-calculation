package com.opportunitycost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 다안 비교 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiComparisonResponse {
    /**
     * 각 선택지의 계산 결과
     */
    private List<OptionResult> results;
    
    /**
     * 추천 선택지 번호 (1부터 시작, 동일한 경우 null)
     */
    private Integer recommendedOption;
    
    /**
     * 최소 총 비용
     */
    private Long minTotalCost;
    
    /**
     * 최대 총 비용
     */
    private Long maxTotalCost;
    
    /**
     * 최대 차액 (최대 비용 - 최소 비용)
     */
    private Long maxDifference;
    
    /**
     * 계산식 설명
     */
    private String formula;
}
