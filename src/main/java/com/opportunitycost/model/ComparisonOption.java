package com.opportunitycost.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비교 선택지 모델
 * 소요 시간(분)과 직접 비용(원)을 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonOption {
    /**
     * 소요 시간 (분)
     */
    @NotNull(message = "소요 시간은 필수 입력 항목입니다.")
    @Min(value = 0, message = "소요 시간은 0분 이상이어야 합니다.")
    private Integer timeMinutes;
    
    /**
     * 직접 비용 (원)
     */
    @NotNull(message = "직접 비용은 필수 입력 항목입니다.")
    @Min(value = 0, message = "직접 비용은 0원 이상이어야 합니다.")
    private Long directCost;
}
