package com.equityledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.equityledger.domain.LedgerEntry;

import jakarta.persistence.LockModeType;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, String> {
    List<LedgerEntry> findByAccountId(String accountId);
    List<LedgerEntry> findByTransactionId(String transactionId);
    // Idempotency lookup
    List<LedgerEntry> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);

    // Pessimistic Write Lock to prevent concurrent modification during balance/holding validation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM LedgerEntry e WHERE e.accountId = :accountId")
    List<LedgerEntry> findByAccountIdWithLock(String accountId);
}