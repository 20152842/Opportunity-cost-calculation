package com.opportunitycost.dto;

import com.opportunitycost.model.ComparisonOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 기회비용 계산 요청 DTO
 */
@Data
public class CalculationRequest {
    /**
     * 시급 (원/시간)
     */
    @NotNull(message = "시급은 필수 입력 항목입니다.")
    @Min(value = 1, message = "시급은 1원 이상이어야 합니다.")
    private Long hourlyWage;
    
    /**
     * 선택지 A
     */
    @NotNull(message = "선택지 A는 필수 입력 항목입니다.")
    @Valid
    private ComparisonOption optionA;
    
    /**
     * 선택지 B
     */
    @NotNull(message = "선택지 B는 필수 입력 항목입니다.")
    @Valid
    private ComparisonOption optionB;
}
