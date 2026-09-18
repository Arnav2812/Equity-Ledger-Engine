# 🛡️ Equity Ledger IAM Architecture

The Equity Ledger Engine utilizes a stateless, JWT-driven Identity and Access Management (IAM) layer integrated directly into the Spring Security Filter Chain. This ensures strict authorization without coupling security logic to the `LedgerController` or double-entry engine.

## 👥 Role & Permissions Model

Access is governed by three hierarchical roles enforced at the endpoint level via `SecurityConfig`:

| Role       | Allowed Operations                                                                                 | Denied Operations |
| :---       | :---                                                                                               | :--- |
| **ADMIN**  | Full Access (Trades, Corporate Actions, Reports, User Management)                                  | None |
| **OPERATOR**| Ledger executions (`POST /api/ledger/trade`) and view operations (`GET /api/ledger/**`)            | Corporate actions, user management |
| **VIEWER** | Read-only ledger and portfolio queries (`GET /api/ledger/**`)                                      | All write/execution endpoints (`403 Forbidden`) |

## 🔑 Token Lifecycle

1. **Login:** Send credentials to `POST /api/auth/login`. Returns an **Access Token** (15-min expiry) containing `role` claims, and a **Refresh Token** (7-day expiry).
2. **Usage:** Append the Access Token to HTTP requests: `Authorization: Bearer <TOKEN>`.
3. **Refresh:** Before expiry, send the Refresh Token to `POST /api/auth/refresh` to obtain a new 15-minute Access Token without re-authenticating.

## 🛑 Security Error Contracts
* **401 Unauthorized:** Returned by `JwtAuthEntryPoint` when a JWT is missing, tampered, or expired.
* **403 Forbidden:** Returned by `CustomAccessDeniedHandler` when a valid token attempts an operation outside its role (e.g., a `VIEWER` hitting the `/trade` endpoint).

## ➕ Adding Protected Endpoints
To protect a new endpoint, do **not** use controller annotations. Navigate to `SecurityConfig.java` and register the route mapping to enforce global architecture consistency:
```java
// Example: Adding a new strictly ADMIN endpoint
.requestMatchers(HttpMethod.POST, "/api/admin/users").hasRole("ADMIN")