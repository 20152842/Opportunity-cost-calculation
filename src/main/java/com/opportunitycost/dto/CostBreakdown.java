package com.opportunitycost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비용 분해 정보
 * 직접 비용과 시간 비용을 분리하여 표시
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostBreakdown {
    /**
     * 직접 비용 (원)
     */
    private Long directCost;
    
    /**
     * 시간 비용 (원)
     */
    private Long timeCost;
    
    /**
     * 총 비용 (원)
     */
    private Long totalCost;
}
