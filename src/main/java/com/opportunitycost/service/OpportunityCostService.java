package com.opportunitycost.service;

import com.opportunitycost.dto.*;
import com.opportunitycost.model.ComparisonOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 기회비용 계산 서비스
 * 
 * 계산식: TC = C + (W/60 × T)
 * - TC: 총 비용 (원)
 * - C: 직접 비용 (원)
 * - W: 시급 (원/시간)
 * - T: 소요 시간 (분)
 */
@Service
public class OpportunityCostService {

    private static final Logger logger = LoggerFactory.getLogger(OpportunityCostService.class);
    
    private final CalculationCacheService cacheService;
    
    // 큰 값 입력 경고 기준 (시급 1,000,000원 이상 또는 총 비용 10,000,000원 이상)
    private static final long WAGE_WARNING_THRESHOLD = 1_000_000L;
    private static final long TOTAL_COST_WARNING_THRESHOLD = 10_000_000L;

    public OpportunityCostService(CalculationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 기회비용을 계산하여 두 선택지를 비교합니다.
     * 
     * @param request 계산 요청 정보
     * @return 계산 결과
     */
    public CalculationResponse calculate(CalculationRequest request) {
        // 캐시 확인
        CalculationResponse cached = cacheService.getCachedResult(request);
        if (cached != null) {
            logger.info("캐시된 결과 반환");
            return cached;
        }
        
        logger.info("기회비용 계산 요청 - 시급: {}, 선택지A: {}원/{}분, 선택지B: {}원/{}분",
            request.getHourlyWage(),
            request.getOptionA().getDirectCost(), request.getOptionA().getTimeMinutes(),
            request.getOptionB().getDirectCost(), request.getOptionB().getTimeMinutes());
        
        // 큰 값 입력 경고
        if (request.getHourlyWage() >= WAGE_WARNING_THRESHOLD) {
            logger.warn("비현실적으로 높은 시급 입력: {}원/시간", request.getHourlyWage());
        }
        Long hourlyWage = request.getHourlyWage();
        
        // 선택지 A 계산
        CostBreakdown optionA = calculateCostBreakdown(
            request.getOptionA().getDirectCost(),
            request.getOptionA().getTimeMinutes(),
            hourlyWage
        );
        
        // 선택지 B 계산
        CostBreakdown optionB = calculateCostBreakdown(
            request.getOptionB().getDirectCost(),
            request.getOptionB().getTimeMinutes(),
            hourlyWage
        );
        
        // 추천 선택지 결정
        String recommendation = determineRecommendation(optionA.getTotalCost(), optionB.getTotalCost());
        
        // 차액 계산 (절댓값)
        Long costDifference = Math.abs(optionA.getTotalCost() - optionB.getTotalCost());
        
        // 계산식 설명 생성
        String formula = generateFormula(hourlyWage);
        
        // 큰 총 비용 경고
        if (optionA.getTotalCost() >= TOTAL_COST_WARNING_THRESHOLD || 
            optionB.getTotalCost() >= TOTAL_COST_WARNING_THRESHOLD) {
            logger.warn("비현실적으로 높은 총 비용 계산됨 - A: {}원, B: {}원", 
                optionA.getTotalCost(), optionB.getTotalCost());
        }
        
        logger.info("계산 완료 - 추천: {}, 차액: {}원", recommendation, costDifference);
        
        CalculationResponse response = new CalculationResponse(optionA, optionB, recommendation, costDifference, formula);
        
        // 결과 캐싱
        cacheService.cacheResult(request, response);
        
        return response;
    }
    
    /**
     * 비용 분해를 계산합니다.
     * 
     * @param directCost 직접 비용 (원)
     * @param timeMinutes 소요 시간 (분)
     * @param hourlyWage 시급 (원/시간)
     * @return 비용 분해 정보
     */
    private CostBreakdown calculateCostBreakdown(Long directCost, Integer timeMinutes, Long hourlyWage) {
        // 시간 비용 계산: (시급 / 60) × 소요 시간(분)
        // 소수점 처리를 위해 double로 계산 후 floor 처리
        double timeCostDouble = (hourlyWage / 60.0) * timeMinutes;
        Long timeCost = (long) Math.floor(timeCostDouble);
        
        // 총 비용 계산: 직접 비용 + 시간 비용
        Long totalCost = directCost + timeCost;
        
        return new CostBreakdown(directCost, timeCost, totalCost);
    }
    
    /**
     * 추천 선택지를 결정합니다.
     * 
     * @param totalCostA 선택지 A의 총 비용
     * @param totalCostB 선택지 B의 총 비용
     * @return "A", "B", 또는 "동일"
     */
    private String determineRecommendation(Long totalCostA, Long totalCostB) {
        if (totalCostA < totalCostB) {
            return "A";
        } else if (totalCostB < totalCostA) {
            return "B";
        } else {
            return "동일";
        }
    }
    
    /**
     * 다안 비교를 수행합니다 (3~5개 선택지).
     * 
     * @param request 다안 비교 요청 정보
     * @return 다안 비교 결과
     */
    public MultiComparisonResponse calculateMulti(MultiComparisonRequest request) {
        logger.info("다안 비교 요청 - 시급: {}, 선택지 개수: {}", 
            request.getHourlyWage(), request.getOptions().size());
        
        Long hourlyWage = request.getHourlyWage();
        List<ComparisonOption> options = request.getOptions();
        
        // 각 선택지의 비용 계산
        List<OptionResult> results = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            ComparisonOption option = options.get(i);
            CostBreakdown breakdown = calculateCostBreakdown(
                option.getDirectCost(),
                option.getTimeMinutes(),
                hourlyWage
            );
            
            OptionResult result = new OptionResult(
                i + 1,
                "선택지 " + (char)('A' + i),
                breakdown
            );
            results.add(result);
        }
        
        // 총 비용 기준으로 정렬
        results.sort(Comparator.comparing(r -> r.getBreakdown().getTotalCost()));
        
        // 최소/최대 비용 및 추천 선택지 결정
        Long minTotalCost = results.get(0).getBreakdown().getTotalCost();
        Long maxTotalCost = results.get(results.size() - 1).getBreakdown().getTotalCost();
        Long maxDifference = maxTotalCost - minTotalCost;
        
        // 최소 비용인 선택지가 여러 개인지 확인
        Integer recommendedOption = null;
        long firstCost = results.get(0).getBreakdown().getTotalCost();
        if (results.stream().filter(r -> r.getBreakdown().getTotalCost().equals(firstCost)).count() == 1) {
            recommendedOption = results.get(0).getOptionNumber();
        }
        
        // 계산식 설명 생성
        String formula = generateFormula(hourlyWage);
        
        logger.info("다안 비교 완료 - 추천 선택지: {}, 최소 비용: {}원, 최대 비용: {}원", 
            recommendedOption, minTotalCost, maxTotalCost);
        
        return new MultiComparisonResponse(results, recommendedOption, minTotalCost, maxTotalCost, maxDifference, formula);
    }
    
    /**
     * 계산식 설명을 생성합니다.
     * 
     * @param hourlyWage 시급 (원/시간)
     * @return 계산식 설명 문자열
     */
    private String generateFormula(Long hourlyWage) {
        double perMinuteValue = hourlyWage / 60.0;
        return String.format(
            "총 비용 = 직접 비용 + (시급 ÷ 60) × 소요 시간(분)\n" +
            "분당 가치 = %,.0f원/분\n" +
            "예: 직접 비용 3,000원 + (%,d원/시간 ÷ 60) × 10분 = 3,000원 + %,.0f원 = 총 비용",
            perMinuteValue, hourlyWage, perMinuteValue * 10
        );
    }
}
