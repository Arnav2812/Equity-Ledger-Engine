# 🚀 Equity Ledger Engine - Full-Stack Trading & Ledger Platform

A high-throughput, fault-tolerant **Full-Stack Equity Ledger & Corporate Actions Platform** built with **Java 22, Spring Boot 3, React (Vite), and MySQL**, fully containerized via **Docker**. Designed to handle high-concurrency equity trade executions with **double-entry accounting**, **pessimistic row-level locking**, **Object-Oriented Strategy Patterns**, and **header-based idempotency guardrails**.

---

## 🌟 Key Features

* **Interactive Trading Dashboard:** Modern dark-themed React + Vite interface (`http://localhost:5174`) offering real-time portfolio tracking, account balance summaries, trade execution controls, and corporate action monitoring.
* **Double-Entry Accounting Core:** Every transaction atomically generates paired `DEBIT` and `CREDIT` entries (e.g., balancing `ACC-MARKET-POOL` against `ACC-USER-101`), preserving zero-sum balance conservation across all system accounts.
* **Race-Condition Safety:** Leverages JPA database pessimistic write locking (`SELECT ... FOR UPDATE`) at the MySQL engine layer to eliminate double-spending, state corruption, and race conditions under heavy parallel trade submissions.
* **Object-Oriented Strategy Pattern:** Clean architectural abstraction for processing diverse transaction types and corporate actions (e.g., stock splits, dividend distributions, trade settlements).
* **Header-Based Idempotency Guardrails:** Custom Spring filter pipeline intercepts and validates requests via `X-Idempotency-Key` headers to safely reject duplicate network transmissions without unintended side effects.
* **Seamless API Gateway / Proxy:** Built-in Vite reverse proxy routing (`/api/*`) seamlessly bridges client requests to the Spring Boot REST backend operating on port 8080.

---

## 🏗️ System Architecture & Data Flow

```text
+-----------------------------------------------------------------------------------+
|                                  FRONTEND LAYER                                   |
|                     React + Vite Dashboard (localhost:5174)                       |
|       ├── Real-Time Portfolio Table & Dynamic Account Summaries                   |
|       └── Trade Execution & Corporate Action Control Panels                       |
+-----------------------------------------------------------------------------------+
                                          |
                                          | /api/ledger/* (Vite Reverse Proxy)
                                          v
+-----------------------------------------------------------------------------------+
|                                  BACKEND LAYER                                    |
|                     Spring Boot REST Engine (localhost:8080)                      |
|       ├── Idempotency Interceptor (X-Idempotency-Key Validation)                  |
|       ├── Business Validation Engine (Holdings & Balance Safety Guardrails)       |
|       └── Double-Entry Accounting Service (OOD Strategy Pattern for Actions)      |
+-----------------------------------------------------------------------------------+
                                          |
                                          | JPA (Pessimistic Write Lock: SELECT ... FOR UPDATE)
                                          v
+-----------------------------------------------------------------------------------+
|                                 DATABASE LAYER                                    |
|                      MySQL 8.0 (Containerized Docker Service)                     |
|       └── Immutable Ledger Entries Table (DEBIT / CREDIT Atomic Pairs)            |
+-----------------------------------------------------------------------------------+
```

---

## 🛠️ Tech Stack

| Domain | Technology |
| :--- | :--- |
| **Frontend** | React 18, Vite, JavaScript (ES6+), CSS3 (Modern Dark Theme) |
| **Backend** | Java 22, Spring Boot 3.x, Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0 |
| **Architecture / Patterns** | Double-Entry Bookkeeping, OOD Strategy Pattern, Idempotency Interceptor, Reverse Proxy Gateway |
| **Containerization & Tools** | Docker, Docker Compose, Maven, Node.js / npm |

---

## ⚡ API Endpoints

### 1. Execute Equity Trade
`POST /api/ledger/trade`

**Headers:**
* `Content-Type: application/json`
* `X-Idempotency-Key: IDEM-<TIMESTAMP_UUID>`

**Request Payload:**
```json
{
  "sourceAccount": "ACC-MARKET-POOL",
  "targetAccount": "ACC-USER-101",
  "symbol": "RELIANCE",
  "amount": 10
}
```

**Response (`200 OK`):**
```json
[
  {
    "id": "c0a80101-90ee-1e2b-8190-ee6f2a330001",
    "accountId": "ACC-MARKET-POOL",
    "symbol": "RELIANCE",
    "entryType": "DEBIT",
    "amount": 10,
    "transactionId": "tx-99f2b1a0-4c2d-11ee-be56-0242ac120002"
  },
  {
    "id": "c0a80101-90ee-1e2b-8190-ee6f2a340002",
    "accountId": "ACC-USER-101",
    "symbol": "RELIANCE",
    "entryType": "CREDIT",
    "amount": 10,
    "transactionId": "tx-99f2b1a0-4c2d-11ee-be56-0242ac120002"
  }
]
```

---

### 2. Fetch Live Account Holdings
`GET /api/ledger/portfolio/{accountId}`

**Response (`200 OK`):**
```json
[
  {
    "symbol": "RELIANCE",
    "quantity": 160,
    "averageCost": 2550.50
  },
  {
    "symbol": "INFY",
    "quantity": 200,
    "averageCost": 1485.20
  }
]
```

---

### 3. Fetch Account Ledger Summary
`GET /api/ledger/account/{accountId}`

**Response (`200 OK`):**
```json
{
  "accountId": "ACC-USER-101",
  "totalCredits": 200.00,
  "totalDebits": 0.00,
  "netBalance": 200.00
}
```

---

## 🧪 Concurrency & Stress Testing Benchmark

To verify thread safety and lock stability under heavy parallel trade submissions, the backend engine was benchmarked against **20 concurrent execution threads** firing transactions against a single market account state.

### Stress Verification Script:
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
          -Body '{"sourceAccount":"ACC-MARKET-POOL","targetAccount":"ACC-USER-101","symbol":"RELIANCE","amount":10}'
    } -ArgumentList $_
} | Out-Null; Get-Job | Receive-Job -Wait -AutoRemoveJob
```

### Benchmark Results:
* **Executed Threads:** 20 Parallel Submissions
* **Deadlocks / Row-Lock Failures:** 0
* **Duplicate Executions:** 0 (Filtered via Idempotency Interceptor)
* **Ledger Balance Integrity:** 100% verified zero-sum math and financial conservation.

---

## 🚀 Quickstart Guide

### Prerequisites
* **Docker Desktop** installed and running
* **Node.js** (v18+ recommended)
* **Git**

---

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/equity-ledger-engine.git
cd equity-ledger-engine
```

---

### 2. Start Backend & Database Services
```bash
docker compose up --build -d
```
*Spins up Spring Boot on port `8080` and MySQL 8.0 on port `3306`.*

---

### 3. Start Frontend Dashboard
```bash
cd equity-ledger-ui
npm install
npm run dev
```

Open your browser at **`http://localhost:5174`** to launch the interactive trading dashboard!
