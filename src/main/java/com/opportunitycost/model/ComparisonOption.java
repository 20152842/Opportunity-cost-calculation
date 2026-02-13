package com.opportunitycost.model;

import jakarta.validation.constraints.Max;
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
     * 상한: 10,080분 (7일, 비현실적으로 긴 시간 방지)
     */
    @NotNull(message = "소요 시간은 필수 입력 항목입니다.")
    @Min(value = 0, message = "소요 시간은 0분 이상이어야 합니다.")
    @Max(value = 10_080, message = "소요 시간은 10,080분(7일) 이하로 입력해주세요.")
    private Integer timeMinutes;
    
    /**
     * 직접 비용 (원)
     * 상한: 1억원 (비현실적으로 높은 비용 방지)
     */
    @NotNull(message = "직접 비용은 필수 입력 항목입니다.")
    @Min(value = 0, message = "직접 비용은 0원 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "직접 비용은 1억원 이하로 입력해주세요.")
    private Long directCost;
}
