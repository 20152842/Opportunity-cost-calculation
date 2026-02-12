package com.opportunitycost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 선택지별 계산 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionResult {
    /**
     * 선택지 번호 (1부터 시작)
     */
    private Integer optionNumber;
    
    /**
     * 선택지 이름 (예: "선택지 A", "선택지 1")
     */
    private String optionName;
    
    /**
     * 비용 분해 정보
     */
    private CostBreakdown breakdown;
}
