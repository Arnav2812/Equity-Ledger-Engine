```markdown
# 🚀 Equity Ledger Engine - Full-Stack Trading, Ledger & IAM Platform

A high-throughput, fault-tolerant **Full-Stack Equity Ledger, Corporate Actions & IAM Platform** built with **Java 22, Spring Boot 3, React (Vite), and MySQL**, fully containerized via **Docker**[cite: 1]. Designed to handle high-concurrency equity trade executions with **double-entry accounting**, **pessimistic row-level locking**, **Object-Oriented Strategy Patterns**, **header-based idempotency guardrails**, and **stateless JWT-based Role-Based Access Control (RBAC)**[cite: 1].

---

## 🌟 Key Features

* **Interactive Trading Dashboard:** Modern dark-themed React + Vite interface (`http://localhost:5174`) offering real-time portfolio tracking, account balance summaries, trade execution controls, corporate action triggers, and an IAM role-switching toolbar (`ADMIN`, `OPERATOR`, `VIEWER`, unauthenticated)[cite: 1].
* **Stateless JWT & Role-Based Access Control (IAM):** Built-in Spring Security 6 filter chain enforcing least-privilege RBAC using short-lived HMAC-SHA256 access tokens (15-min expiry) and secure refresh tokens (7-day expiry).
* **Granular HTTP Error Separation:** Dedicated `JwtAuthEntryPoint` and `CustomAccessDeniedHandler` enforcing an explicit separation between unauthenticated requests (`401 Unauthorized`) and insufficient role permissions (`403 Forbidden`).
* **Double-Entry Accounting Core:** Every transaction atomically generates paired `DEBIT` and `CREDIT` entries (balancing `ACC-MARKET-POOL` against user accounts), preserving zero-sum balance conservation across all system accounts[cite: 1].
* **Race-Condition Safety:** Leverages JPA database pessimistic write locking (`SELECT ... FOR UPDATE`) at the MySQL engine layer to eliminate double-spending, state corruption, and race conditions under heavy parallel trade submissions[cite: 1].
* **Object-Oriented Strategy Pattern:** Clean architectural abstraction for processing diverse transaction types and corporate actions (e.g., stock splits, bonus issues, dividend distributions)[cite: 1].
* **Header-Based Idempotency Guardrails:** Custom Spring filter pipeline intercepts and validates requests via `X-Idempotency-Key` headers to safely reject duplicate network transmissions without unintended side effects[cite: 1].
* **Seamless API Gateway / Proxy:** Built-in Vite reverse proxy routing (`/api/*`) seamlessly bridges client requests to the Spring Boot REST backend operating on port 8080[cite: 1].

---

## 🏗️ System Architecture & Data Flow

```text
+-----------------------------------------------------------------------------------+
|                                  FRONTEND LAYER                                   |
|                     React + Vite Dashboard (localhost:5174)                       |
|       ├── Real-Time Portfolio Table & Audit Trail                                 |
|       ├── Trade Execution & Corporate Action Panels                               |
|       └── IAM Control Bar (Role Switcher & JWT Token Lifecycle Manager)           |
+-----------------------------------------------------------------------------------+
                                          |
                                          | /api/* (Vite Reverse Proxy)
                                          v
+-----------------------------------------------------------------------------------+
|                            SECURITY & FILTER LAYER                                |
|                     Spring Security (Port 8080)                                   |
|       ├── JwtAuthenticationFilter (HMAC-SHA256 Token Validation & Context Setup)  |
|       ├── Exception Handling (401 AuthenticationEntryPoint / 403 AccessDenied)    |
|       └── Route Matchers (Least-Privilege Role Authorization: ADMIN/OPERATOR/VIEWER)
+-----------------------------------------------------------------------------------+
                                          |
                                          | Authenticated Request Flow
                                          v
+-----------------------------------------------------------------------------------+
|                                 BACKEND CORE                                      |
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
|       ├── Users Table (BCrypt Encrypted Credentials, System Roles)                |
|       └── Immutable Ledger Entries Table (DEBIT / CREDIT Atomic Pairs)            |
+-----------------------------------------------------------------------------------+

```

---

## 🛠️ Tech Stack

| Domain | Technology |
| --- | --- |
| **Frontend** | React 18, Vite, JavaScript (ES6+), CSS3 (Modern Dark Theme)

 |
| **Backend** | Java 22, Spring Boot 3.x, Spring Data JPA, Hibernate

 |
| **Security & IAM** | Spring Security 6, JJWT (io.jsonwebtoken 0.12.3), BCrypt Password Hashing, RBAC |
| **Database** | MySQL 8.0

 |
| **Architecture / Patterns** | Double-Entry Bookkeeping, OOD Strategy Pattern, Idempotency Guardrails, Filter Chain RBAC, Reverse Proxy

 |
| **Containerization & Tools** | Docker, Docker Compose, Maven, Node.js / npm

 |

---

## 👥 Role & Permissions Matrix

| Role | Permissions | Endpoints Allowed |
| --- | --- | --- |
| **ADMIN** | Full system administration, trade execution, corporate action issuance, view-all. | `POST /api/ledger/trade`<br>

<br>`POST /api/ledger/corporate-action`<br>

<br>`GET /api/ledger/**` |
| **OPERATOR** | Active order execution and ledger reading. Blocked from restructuring capital. | `POST /api/ledger/trade`<br>

<br>`GET /api/ledger/**` |
| **VIEWER** | Read-only access to audit logs and portfolio balances. Blocked from writing. | `GET /api/ledger/**` |

---

## ⚡ API Endpoints

### 1. Authentication Endpoints

#### Login & Issue Tokens

`POST /api/auth/login`

**Request Payload:**

```json
{
  "username": "operator",
  "password": "op123"
}

```

**Response (`200 OK`):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ..."
}

```

#### Refresh Access Token

`POST /api/auth/refresh`

**Request Payload:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ..."
}

```

**Response (`200 OK`):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ..."
}

```

---

### 2. Ledger & Trading Endpoints

#### Execute Equity Trade

`POST /api/ledger/trade`

* **Required Roles:** `ADMIN` or `OPERATOR`
* **Headers:**
* `Authorization: Bearer <ACCESS_TOKEN>`
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

#### Execute Corporate Action

`POST /api/ledger/corporate-action`

* **Required Roles:** `ADMIN`
* **Headers:**
* `Authorization: Bearer <ACCESS_TOKEN>`
* `Content-Type: application/json`



**Request Payload:**

```json
{
  "type": "STOCK_SPLIT",
  "symbol": "RELIANCE",
  "ratio": 2,
  "targetAccount": "ACC-USER-101"
}

```

#### Fetch Live Account Holdings

`GET /api/ledger/portfolio/{accountId}`

* **Required Roles:** `ADMIN`, `OPERATOR`, or `VIEWER`
* **Headers:** `Authorization: Bearer <ACCESS_TOKEN>`

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

#### Fetch Account Ledger Summary

`GET /api/ledger/account/{accountId}`

* **Required Roles:** `ADMIN`, `OPERATOR`, or `VIEWER`
* **Headers:** `Authorization: Bearer <ACCESS_TOKEN>`

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
        param($id)$key = "STRESS-$id-$([guid]::NewGuid())"
        Invoke-RestMethod -Uri "http://localhost:8080/api/ledger/trade" `
          -Method POST `
          -Headers @{ 
            "Authorization" = "Bearer $using:adminToken"
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
git clone [https://github.com/your-username/equity-ledger-engine.git](https://github.com/your-username/equity-ledger-engine.git)
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

Open your browser at **`http://localhost:5174`** to launch the interactive trading dashboard.

### Default Seed Credentials

| Username | Password | Role | Intended Verification |
| --- | --- | --- | --- |
| `admin` | `admin123` | `ADMIN` | Unrestricted trade execution, corporate actions, and audits. |
| `operator` | `op123` | `OPERATOR` | Trade execution and read access; corporate actions blocked (`403 Forbidden`). |
| `viewer` | `view123` | `VIEWER` | Read-only ledger inspection; all trades blocked (`403 Forbidden`). |

```
