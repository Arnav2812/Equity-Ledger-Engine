package com.equityledger.strategy;

import java.math.BigDecimal;
import java.util.List;

import com.equityledger.domain.LedgerEntry;

public interface CorporateActionStrategy {
    CorporateActionType getType();
    /**
     * Executes the corporate action and returns the adjustments to be persisted in the ledger.
     */
    List<LedgerEntry> execute(String transactionId, String accountId, String symbol, BigDecimal currentShares, BigDecimal ratio);
}