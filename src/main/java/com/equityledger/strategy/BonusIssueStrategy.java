package com.equityledger.strategy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.equityledger.domain.EntryType;
import com.equityledger.domain.LedgerEntry;

@Component
public class BonusIssueStrategy implements CorporateActionStrategy {

    @Override
    public CorporateActionType getType() {
        return CorporateActionType.BONUS_ISSUE;
    }

    @Override
    public List<LedgerEntry> execute(String transactionId, String accountId, String symbol, BigDecimal currentShares, BigDecimal ratio) {
        if (currentShares.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }

        // For a 1:1 bonus issue (ratio = 1.0), bonus shares issued = currentShares * 1.0
        BigDecimal bonusShares = currentShares.multiply(ratio);

        LedgerEntry bonusAdjustment = new LedgerEntry(
                transactionId,
                accountId,
                symbol,
                EntryType.CREDIT,
                bonusShares
        );

        return List.of(bonusAdjustment);
    }
}