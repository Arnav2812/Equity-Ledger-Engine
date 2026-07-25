package com.equityledger.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equityledger.domain.CorporateActionRequest;
import com.equityledger.domain.LedgerEntry;
import com.equityledger.domain.PortfolioHolding;
import com.equityledger.service.LedgerService;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/trade")
    public ResponseEntity<List<LedgerEntry>> recordTrade(
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
        @RequestBody TradeRequest request) {

    List<LedgerEntry> entries = ledgerService.processTradeWithIdempotency(request, idempotencyKey);
    return ResponseEntity.ok(entries);
}

    @PostMapping("/corporate-action")
    public ResponseEntity<String> executeCorporateAction(@RequestBody CorporateActionRequest request) {
    String txId = ledgerService.processCorporateAction(request);
    return ResponseEntity.ok("Corporate action processed successfully. TxID: " + txId);
}

    @GetMapping("/portfolio/{accountId}")
    public ResponseEntity<List<PortfolioHolding>> getPortfolio(@PathVariable String accountId) {
        return ResponseEntity.ok(ledgerService.getPortfolioHoldings(accountId));
}

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LedgerEntry>> getAccountHistory(@PathVariable String accountId) {
        return ResponseEntity.ok(ledgerService.getAccountHistory(accountId));
    }

    public static class TradeRequest {
        private String sourceAccount;
        private String targetAccount;
        private String symbol;
        private BigDecimal amount;

        public String getSourceAccount() { return sourceAccount; }
        public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }
        public String getTargetAccount() { return targetAccount; }
        public void setTargetAccount(String targetAccount) { this.targetAccount = targetAccount; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}