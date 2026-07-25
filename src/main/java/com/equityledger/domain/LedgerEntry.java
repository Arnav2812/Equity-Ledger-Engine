package com.equityledger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    private String id;

    @Column(name = "idempotency_key", nullable = true)
    private String idempotencyKey;

    @Column(name = "transaction_id",nullable = false)
    private String transactionId;

    @Column(name = "account_id",nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type",nullable = false)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    // Default constructor required by JPA
    public LedgerEntry() {}

    // 2. 5-arg constructor (Auto-generates ID, null idempotencyKey)
    public LedgerEntry(String transactionId, String accountId, String symbol, EntryType entryType, BigDecimal amount) {
        this(UUID.randomUUID().toString(), transactionId, accountId, symbol, entryType, amount, null);
    }

    // 3. 6-arg constructor (Auto-generates ID with idempotencyKey)
    public LedgerEntry(String transactionId, String accountId, String symbol, EntryType entryType, BigDecimal amount, String idempotencyKey) {
        this(UUID.randomUUID().toString(), transactionId, accountId, symbol, entryType, amount, idempotencyKey);
    }

    // 4. 7-arg constructor (Full explicit construction)
    public LedgerEntry(String id, String transactionId, String accountId, String symbol, EntryType entryType, BigDecimal amount, String idempotencyKey) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.symbol = symbol;
        this.entryType = entryType;
        this.amount = amount;
        this.timestamp = Instant.now();
        this.idempotencyKey = idempotencyKey;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}