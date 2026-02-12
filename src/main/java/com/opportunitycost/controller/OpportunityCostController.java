package com.opportunitycost.controller;

import com.opportunitycost.dto.*;
import com.opportunitycost.service.OpportunityCostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 기회비용 계산 컨트롤러
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OpportunityCostController {

    private static final Logger logger = LoggerFactory.getLogger(OpportunityCostController.class);
    
    private final OpportunityCostService opportunityCostService;

    public OpportunityCostController(OpportunityCostService opportunityCostService) {
        this.opportunityCostService = opportunityCostService;
    }

    /**
     * 기회비용 계산 API (2개 선택지 비교)
     * 
     * @param request 계산 요청 정보
     * @return 계산 결과
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculate(@Valid @RequestBody CalculationRequest request) {
        CalculationResponse response = opportunityCostService.calculate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 다안 비교 API (3~5개 선택지 비교)
     * 
     * @param request 다안 비교 요청 정보
     * @return 다안 비교 결과
     */
    @PostMapping("/calculate/multi")
    public ResponseEntity<MultiComparisonResponse> calculateMulti(@Valid @RequestBody MultiComparisonRequest request) {
        MultiComparisonResponse response = opportunityCostService.calculateMulti(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 입력 검증 오류 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("입력 검증 오류 발생: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
