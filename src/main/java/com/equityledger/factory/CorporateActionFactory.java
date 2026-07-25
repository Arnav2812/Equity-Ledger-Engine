package com.equityledger.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.equityledger.strategy.CorporateActionStrategy;
import com.equityledger.strategy.CorporateActionType;

@Component
public class CorporateActionFactory {

    private final Map<CorporateActionType, CorporateActionStrategy> strategyMap;

    public CorporateActionFactory(List<CorporateActionStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(CorporateActionStrategy::getType, Function.identity()));
    }

    public CorporateActionStrategy getStrategy(CorporateActionType type) {
        CorporateActionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No corporate action strategy found for type: " + type);
        }
        return strategy;
    }
}