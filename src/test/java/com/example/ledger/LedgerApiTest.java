package com.example.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 웹 → 유스케이스 → JPA(H2) 까지 실제로 이어 붙인 통합 테스트 */
@SpringBootTest
@AutoConfigureMockMvc
class LedgerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 예산을_정하고_지출을_등록하면_리포트에_반영된다() throws Exception {
        mockMvc.perform(put("/api/budgets/2026-09")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limit\": 50000}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 30000, \"category\": \"식비\", \"spentAt\": \"2026-09-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetExceeded").value(false));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 25000, \"category\": \"교통\", \"spentAt\": \"2026-09-20\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyTotal").value(55000))
                .andExpect(jsonPath("$.budgetExceeded").value(true));

        mockMvc.perform(get("/api/reports/2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(55000))
                .andExpect(jsonPath("$.remaining").value(-5000))
                .andExpect(jsonPath("$.totalByCategory.식비").value(30000))
                .andExpect(jsonPath("$.expenses.length()").value(2));
    }

    @Test
    void 잘못된_금액은_400을_돌려준다() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -1, \"category\": \"식비\", \"spentAt\": \"2026-09-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지출 금액은 0보다 커야 합니다"));
    }
}
