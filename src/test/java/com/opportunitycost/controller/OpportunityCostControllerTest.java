package com.opportunitycost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opportunitycost.dto.CalculationRequest;
import com.opportunitycost.dto.CalculationResponse;
import com.opportunitycost.model.ComparisonOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 기회비용 계산 컨트롤러 통합 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
class OpportunityCostControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("유효한 요청에 대한 성공 응답 테스트")
    void testCalculate_Success() throws Exception {
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(new ComparisonOption(10, 3000L));
        request.setOptionB(new ComparisonOption(40, 2300L));

        String response = mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendation").exists())
                .andExpect(jsonPath("$.optionA").exists())
                .andExpect(jsonPath("$.optionB").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculationResponse result = objectMapper.readValue(response, CalculationResponse.class);
        assertNotNull(result);
        assertEquals("A", result.getRecommendation());
        assertEquals(5500L, result.getOptionA().getTotalCost());
        assertEquals(12300L, result.getOptionB().getTotalCost());
    }

    @Test
    @DisplayName("시급이 null인 경우 검증 오류 테스트")
    void testCalculate_InvalidHourlyWage() throws Exception {
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(null); // null 값
        request.setOptionA(new ComparisonOption(10, 3000L));
        request.setOptionB(new ComparisonOption(40, 2300L));

        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("시급이 0인 경우 검증 오류 테스트")
    void testCalculate_ZeroHourlyWage() throws Exception {
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(0L); // 0 값
        request.setOptionA(new ComparisonOption(10, 3000L));
        request.setOptionB(new ComparisonOption(40, 2300L));

        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("선택지 A가 null인 경우 검증 오류 테스트")
    void testCalculate_NullOptionA() throws Exception {
        CalculationRequest request = new CalculationRequest();
        request.setHourlyWage(15000L);
        request.setOptionA(null); // null 값
        request.setOptionB(new ComparisonOption(40, 2300L));

        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 JSON 형식 요청 테스트")
    void testCalculate_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}
