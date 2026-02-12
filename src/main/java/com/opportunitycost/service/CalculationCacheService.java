package com.opportunitycost.service;

import com.opportunitycost.dto.CalculationRequest;
import com.opportunitycost.dto.CalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 계산 결과 캐싱 서비스
 * 동일한 입력에 대해 캐시된 결과를 반환하여 성능을 향상시킵니다.
 */
@Service
public class CalculationCacheService {

    private static final Logger logger = LoggerFactory.getLogger(CalculationCacheService.class);
    
    // 간단한 인메모리 캐시 (프로덕션에서는 Redis 등을 사용 권장)
    private final Map<String, CalculationResponse> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * 캐시 키 생성
     */
    private String generateCacheKey(CalculationRequest request) {
        return String.format("%d-%d-%d-%d-%d",
            request.getHourlyWage(),
            request.getOptionA().getTimeMinutes(),
            request.getOptionA().getDirectCost(),
            request.getOptionB().getTimeMinutes(),
            request.getOptionB().getDirectCost()
        );
    }

    /**
     * 캐시에서 결과 조회
     */
    public CalculationResponse getCachedResult(CalculationRequest request) {
        String key = generateCacheKey(request);
        CalculationResponse cached = cache.get(key);
        if (cached != null) {
            logger.debug("캐시 히트: {}", key);
            return cached;
        }
        logger.debug("캐시 미스: {}", key);
        return null;
    }

    /**
     * 결과를 캐시에 저장
     */
    public void cacheResult(CalculationRequest request, CalculationResponse response) {
        // 캐시 크기 제한
        if (cache.size() >= MAX_CACHE_SIZE) {
            // 가장 오래된 항목 제거 (간단한 구현)
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            logger.debug("캐시 항목 제거: {}", firstKey);
        }
        
        String key = generateCacheKey(request);
        cache.put(key, response);
        logger.debug("캐시 저장: {}", key);
    }

    /**
     * 캐시 초기화
     */
    public void clearCache() {
        cache.clear();
        logger.info("캐시 초기화 완료");
    }

    /**
     * 캐시 크기 조회
     */
    public int getCacheSize() {
        return cache.size();
    }
}
