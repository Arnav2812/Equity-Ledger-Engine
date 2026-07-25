package com.equityledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PortfolioHolding {
    private final String symbol;
    private long quantity;
    private BigDecimal averageCost;

    public PortfolioHolding(String symbol, long quantity, BigDecimal averageCost) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost.setScale(4, RoundingMode.HALF_UP);
    }

    public String getSymbol() { return symbol; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { 
        this.averageCost = averageCost.setScale(4, RoundingMode.HALF_UP); 
    }

    @Override
    public String toString() {
        return String.format("%s -> Shares: %d, Avg Cost: ₹%s", symbol, quantity, averageCost);
    }
}