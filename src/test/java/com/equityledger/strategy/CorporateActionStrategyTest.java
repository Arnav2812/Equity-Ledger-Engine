package com.equityledger.strategy;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.equityledger.domain.EntryType;
import com.equityledger.domain.LedgerEntry;

public class CorporateActionStrategyTest {

    @Test
    public void testStockSplitStrategy() {
        StockSplitStrategy strategy = new StockSplitStrategy();

        // 2:1 Split on 100 shares -> Should yield 100 additional shares
        List<LedgerEntry> entries = strategy.execute(
                "TX-SPLIT-001",
                "ACC-101",
                "NVDA",
                BigDecimal.valueOf(100),
                new BigDecimal("2.0")
        );

        assertEquals(1, entries.size());
        assertEquals(EntryType.CREDIT, entries.get(0).getEntryType());
        assertEquals(new BigDecimal("100.0"), entries.get(0).getAmount());
    }

    @Test
    public void testBonusIssueStrategy() {
        BonusIssueStrategy strategy = new BonusIssueStrategy();

        // 1:1 Bonus Issue on 50 shares -> Should yield 50 bonus shares
        List<LedgerEntry> entries = strategy.execute(
                "TX-BONUS-001",
                "ACC-101",
                "AAPL",
                BigDecimal.valueOf(50),
                new BigDecimal("1.0")
        );

        assertEquals(1, entries.size());
        assertEquals(EntryType.CREDIT, entries.get(0).getEntryType());
        assertEquals(new BigDecimal("50.0"), entries.get(0).getAmount());
    }
}