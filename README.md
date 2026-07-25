# 🚀 Equity Ledger Engine

A high-throughput, fault-tolerant **Equity Ledger Engine** built with **Java 22, Spring Boot 3, and MySQL**, fully containerized via **Docker**. Designed to handle concurrent equity trade execution with **double-entry accounting**, **pessimistic row-level locking**, and **header-based idempotency guardrails**.

---

## Key Features

* **Double-Entry Accounting Core:** Every transaction atomically generates paired `DEBIT` and `CREDIT` entries linked by a unique transaction identifier, ensuring balance conservation across all accounts.
* **Race-Condition Safety:** Leverages JPA pessimistic write locking (`SELECT ... FOR UPDATE`) at the database level to prevent double-spending and state corruption under concurrent trade executions.
* **Header-Based Idempotency:** Custom filter pipeline intercepts and validates requests via `X-Idempotency-Key` headers to safely ignore duplicated network requests without side effects.
* **Business Validation Guardrails:** Prevents execution on illegal account states (e.g., balance insufficiency or shorting without position holdings).
* **Dockerized Architecture:** Micro-service setup powered by Docker Compose for rapid spin-up and seamless deployment.

---

## System Architecture & Data Flow

```text
+------------------+         +----------------------------+         +---------------------------+
|   Client / API   | ------> | Idempotency Interceptor    | ------> | Ledger Controller         |
|  Request (JSON)  |         | (X-Idempotency-Key Filter) |         | POST /api/ledger/trade    |
+------------------+         +----------------------------+         +---------------------------+
                                                                                  |
                                                                                  v
+------------------+         +----------------------------+         +---------------------------+
|  MySQL Database  | <------ | JPA Repository             | <------ | Ledger Service            |
| (DEBIT / CREDIT) |         | (Pessimistic Write Lock)   |         | (Double-Entry Validation) |
+------------------+         +----------------------------+         +---------------------------+
```

---

## Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 22 |
| **Framework** | Spring Boot 3.x, Spring Data JPA |
| **Database** | MySQL 8.0 |
| **Containerization** | Docker, Docker Compose |
| **Build Tool** | Maven |

---

## API Endpoints

### 1. Execute Equity Trade
`POST /api/ledger/trade`

**Headers:**
* `Content-Type: application/json`
* `X-Idempotency-Key: <UNIQUE_UUID_KEY>`

**Request Body:**
```json
{
  "sourceAccount": "MARKET_MAKER",
  "targetAccount": "ACC_101",
  "symbol": "RELIANCE",
  "amount": 100.00
}
```

**Response (200 OK):**
```json
{
  "transactionId": "tx-8f4b2a10-9c2b-4d3e-91a5-e214828a2110",
  "status": "SUCCESS",
  "timestamp": "2026-07-25T14:00:00Z"
}
```

---

### 2. Fetch Account Ledger Summary
`GET /api/ledger/account/{accountId}`

**Response (200 OK):**
```json
{
  "accountId": "ACC_101",
  "totalCredits": 200.00,
  "totalDebits": 0.00,
  "netBalance": 200.00
}
```

---

## Concurrency & Stress Testing Benchmark

To verify thread-safety under heavy parallel load, the engine was stress-tested against **20 parallel workers** firing simultaneous execution requests against the exact same source account.

### Verification Run:
```powershell
1..20 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($id)
        $key = "STRESS-$id-$([guid]::NewGuid())"
        Invoke-RestMethod -Uri "http://localhost:8080/api/ledger/trade" `
          -Method POST `
          -Headers @{ 
            "Content-Type" = "application/json"
            "X-Idempotency-Key" = $key
          } `
          -Body '{"sourceAccount":"MARKET_MAKER","targetAccount":"ACC_101","symbol":"RELIANCE","amount":10.00}'
    } -ArgumentList $_
} | Out-Null; Get-Job | Receive-Job -Wait -AutoRemoveJob
```

### Benchmark Results:
* **Processed Requests:** 20 Parallel Executions
* **Deadlocks / Lock Timeouts:** 0
* **Duplicate Executions:** 0
* **Ledger Balance Consistency:** 100% (20 x 10.00 = 200.00 credits cleanly verified).

---

## Quickstart Guide

### Prerequisites
* Docker Desktop installed and running
* Git

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/equity-ledger-engine.git
cd equity-ledger-engine
```

### 2. Build & Launch Containers
```bash
docker-compose up --build -d
```

### 3. Verify System Health
```bash
docker ps
```
Both `equity-ledger-app` and `equity-ledger-db` should report `running` / `healthy` status.
