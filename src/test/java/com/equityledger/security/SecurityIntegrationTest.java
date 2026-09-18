package com.equityledger.security;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.equityledger.service.LedgerService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerService ledgerService;

    private final String tradePayload = "{\"sourceAccount\":\"A\",\"targetAccount\":\"B\",\"symbol\":\"TICKER\",\"amount\":10}";

    @Test
    void unauthenticatedUser_Returns401() throws Exception {
        mockMvc.perform(post("/api/ledger/trade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tradePayload))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerRole_ExecutingTrade_Returns403() throws Exception {
        mockMvc.perform(post("/api/ledger/trade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tradePayload))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerRole_FetchingPortfolio_Returns200() throws Exception {
        when(ledgerService.getPortfolioHoldings("ACC-101")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/ledger/portfolio/ACC-101"))
                .andExpect(status().isOk()); // 200
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_ExecutingTrade_Returns200() throws Exception {
        when(ledgerService.processTradeWithIdempotency(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/ledger/trade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tradePayload))
                .andExpect(status().isOk()); // 200
    }
}