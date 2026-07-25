package com.equityledger.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.equityledger.controller.LedgerController.TradeRequest;
import com.equityledger.domain.CorporateActionRequest;
import com.equityledger.domain.EntryType;
import com.equityledger.domain.LedgerEntry;
import com.equityledger.domain.PortfolioHolding;
import com.equityledger.factory.CorporateActionFactory;
import com.equityledger.repository.LedgerRepository;
import com.equityledger.strategy.CorporateActionStrategy;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<LedgerEntry> processTradeWithIdempotency(TradeRequest request, String idempotencyKey) {

    // 1. Guardrail Check: Skip check for system accounts (e.g., MARKET_MAKER)
    if (!"MARKET_MAKER".equalsIgnoreCase(request.getSourceAccount())) {
        Long currentShares = getPortfolioHoldings(request.getSourceAccount()).stream()
                .filter(holding -> holding.getSymbol().equalsIgnoreCase(request.getSymbol()))
                .map(PortfolioHolding::getQuantity)
                .findFirst()
                .orElse(0L);

        // Convert requested amount to shares for comparison
        long requestedShares = request.getAmount().longValue();

        if (currentShares < requestedShares) {
            throw new IllegalStateException(String.format(
                "Insufficient holdings for %s in %s. Available: %d shares, Requested: %d shares",
                request.getSourceAccount(), request.getSymbol(), currentShares, requestedShares
            ));
        }
    }
    
    // 1. Check if this request was already processed
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
        List<LedgerEntry> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (!existing.isEmpty()) {
            // Return cached result immediately without re-executing business logic
            return existing; 
        }
    }

    // 2. Lock source account records to prevent race conditions during concurrent trades
    List<LedgerEntry> sourceEntries = ledgerRepository.findByAccountIdWithLock(request.getSourceAccount());

    String transactionId = UUID.randomUUID().toString();

    // Create DEBIT entry using constructor
    LedgerEntry debit = new LedgerEntry(
            UUID.randomUUID().toString(),
            transactionId,
            request.getSourceAccount(),
            request.getSymbol(),
            EntryType.DEBIT,
            request.getAmount(),
            idempotencyKey
    );

    // Create CREDIT entry using constructor
    LedgerEntry credit = new LedgerEntry(
            UUID.randomUUID().toString(),
            transactionId,
            request.getTargetAccount(),
            request.getSymbol(),
            EntryType.CREDIT,
            request.getAmount(),
            idempotencyKey
    );

    return ledgerRepository.saveAll(List.of(debit, credit));

    }

    public List<PortfolioHolding> getPortfolioHoldings(String accountId) {
    List<LedgerEntry> entries = ledgerRepository.findByAccountId(accountId);
    Map<String, PortfolioHolding> holdingsMap = new HashMap<>();

    for (LedgerEntry entry : entries) {
        String symbol = entry.getSymbol();
        if (symbol == null || symbol.isBlank()) continue;

        long qty = entry.getAmount().longValue();
        BigDecimal entryPrice = entry.getAmount(); 

        PortfolioHolding holding = holdingsMap.computeIfAbsent(
                symbol, k -> new PortfolioHolding(k, 0L, BigDecimal.ZERO)
        );

        if (entry.getEntryType() == EntryType.CREDIT) {
            long newQty = holding.getQuantity() + qty;
            holding.setQuantity(newQty);
            
            // Set average cost basis if initialized
            if (holding.getAverageCost().compareTo(BigDecimal.ZERO) == 0) {
                holding.setAverageCost(entryPrice);
            }
        } else if (entry.getEntryType() == EntryType.DEBIT) {
            holding.setQuantity(holding.getQuantity() - qty);
        }
    }

    return new ArrayList<>(holdingsMap.values());
}

    /**
     * Atomically records a double-entry transaction.
     * Both Debit and Credit entries are saved together, or neither is saved if an exception occurs.
     */
    @Transactional
    public String recordTrade(String sourceAccount, String targetAccount, String symbol, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();

        LedgerEntry debitEntry = new LedgerEntry(transactionId, sourceAccount, symbol, EntryType.DEBIT, amount);
        LedgerEntry creditEntry = new LedgerEntry(transactionId, targetAccount, symbol, EntryType.CREDIT, amount);

        ledgerRepository.save(debitEntry);
        ledgerRepository.save(creditEntry);

        return transactionId;
    }

    public List<LedgerEntry> getAccountHistory(String accountId) {
        return ledgerRepository.findByAccountId(accountId);
    }

    @Autowired
    private CorporateActionFactory corporateActionFactory;

    @Transactional
    public String processCorporateAction(CorporateActionRequest request) {
    // 1. Fetch current holdings quantity (returns Long)
    Long currentShares = getPortfolioHoldings(request.accountId()).stream()
            .filter(holding -> holding.getSymbol().equalsIgnoreCase(request.symbol()))
            .map(PortfolioHolding::getQuantity)
            .findFirst()
            .orElse(0L);

    BigDecimal currentQuantity = BigDecimal.valueOf(currentShares);

    if (currentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException("Account " + request.accountId() + " has no holdings in " + request.symbol());
    }

    // 2. Resolve Strategy at runtime
    CorporateActionStrategy strategy = corporateActionFactory.getStrategy(request.actionType());
    
    String txId = "CA-" + UUID.randomUUID().toString().substring(0, 8);

    // 3. Execute business logic via strategy
    List<LedgerEntry> adjustments = strategy.execute(txId, request.accountId(), request.symbol(), currentQuantity, request.ratio());

    // 4. Save audit records to ledger
    ledgerRepository.saveAll(adjustments);

    return txId;
}

}