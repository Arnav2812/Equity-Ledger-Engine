package com.equityledger.domain;

import java.math.BigDecimal;

import com.equityledger.strategy.CorporateActionType;

public record CorporateActionRequest(
        String accountId,
        String symbol,
        CorporateActionType actionType,
        BigDecimal ratio
) {}