package com.equityledger.strategy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.equityledger.domain.EntryType;
import com.equityledger.domain.LedgerEntry;

@Component
public class StockSplitStrategy implements CorporateActionStrategy {

    @Override
    public CorporateActionType getType() {
        return CorporateActionType.STOCK_SPLIT;
    }

    @Override
    public List<LedgerEntry> execute(String transactionId, String accountId, String symbol, BigDecimal currentShares, BigDecimal ratio) {
        if (currentShares.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }

        // For a 2:1 split (ratio = 2.0), additional shares = currentShares * (2.0 - 1.0) = currentShares
        BigDecimal additionalShares = currentShares.multiply(ratio.subtract(BigDecimal.ONE));

        LedgerEntry splitAdjustment = new LedgerEntry(
                transactionId,
                accountId,
                symbol,
                EntryType.CREDIT,
                additionalShares
        );

        return List.of(splitAdjustment);
    }
}