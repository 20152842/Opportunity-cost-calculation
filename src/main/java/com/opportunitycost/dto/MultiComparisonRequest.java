package com.opportunitycost.dto;

import com.opportunitycost.model.ComparisonOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 다안 비교 요청 DTO (3~5개 선택지)
 */
@Data
public class MultiComparisonRequest {
    /**
     * 시급 (원/시간)
     */
    @NotNull(message = "시급은 필수 입력 항목입니다.")
    @Min(value = 1, message = "시급은 1원 이상이어야 합니다.")
    private Long hourlyWage;
    
    /**
     * 비교할 선택지 목록 (3~5개)
     */
    @NotNull(message = "선택지는 필수 입력 항목입니다.")
    @Size(min = 3, max = 5, message = "선택지는 3개 이상 5개 이하여야 합니다.")
    @Valid
    private List<ComparisonOption> options;
}
