[![Build](https://github.com/CamilYed/safe-temporary-db-access/actions/workflows/ci.yml/badge.svg)](https://github.com/CamilYed/safe-temporary-db-access/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CamilYed_safe-temporary-db-access&metric=coverage)](https://sonarcloud.io/dashboard?id=CamilYed_safe-temporary-db-access)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=CamilYed_safe-temporary-db-access&metric=alert_status)](https://sonarcloud.io/dashboard?id=CamilYed_safe-temporary-db-access)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CamilYed_safe-temporary-db-access&metric=security_rating)](https://sonarcloud.io/dashboard?id=CamilYed_safe-temporary-db-access)

🔎 [View SonarCloud Dashboard](https://sonarcloud.io/dashboard?id=CamilYed_safe-temporary-db-access)
# Secure and Temporary Access to Production Databases

This project is a **proof of concept (PoC)** for building a secure, temporary access system for production databases, designed for developers and operations teams. It provides **time-limited**, **audited**, and **least-privilege** database access with optional network monitoring.

The core idea: let developers request short-lived database access through a secure API. Access is granted with minimal permissions, and then revoked automatically after a defined TTL.

---

## 💡 Key Principles

- **Security First**: time-limited credentials, no password reuse, audit logs.
- **TDD-Driven Development**: features are always implemented after writing readable, expressive tests.
- **Database-Agnostic Core**: PostgreSQL support first, with extensibility for other databases (e.g., Oracle, MySQL).

---

## ✅ TODO (TDD-first)

### 🔹 Phase 1: Project Setup

- [✅] Initialize Spring Boot + Gradle project
- [✅] Add Spock testing support
- [✅] Write integration test that loads Spring context and asserts it is not null
- [✅] Configure minimal CI (GitHub Actions)

### ✅ Phase 2 – Integration Test Strategy Checklist

## 🔹 Phase 0: Security & Authorization 

- [✅] **Test**: only authorized users can request access (JWT + allowlist)
- [✅] **Code**: implement Spring Security + JWT parser
- [✅] **Test**: Missing JWT → 401 Unauthorized
- [✅] **Test**: Expired or invalid JWT → 401 / 403
- [✅] **Test**: JWT without sub → 400 or 403
- [✅] **Test**: User not in allowlist → 403
- [✅] **Test**: User in allowlist → 200 OK
- [✅] **Code**: JWT parsing & token validator
- [✅] **Code**: AuthorizedUsersRepository reading from YAML/JSON

## 🔹 1. Functional Core Scenarios (Happy Path)

- [ ] 1 – Valid request creates user with `READ_ONLY` permissions
- [ ] 2 – User with `READ_WRITE` gets INSERT, UPDATE rights
- [ ] 3 – User with `DELETE` gets DELETE right
- [ ] 4 – TTL expiration removes user from DB
- [ ] 5 – Response includes username/password only once

---

## 🔹 2. Edge Cases & Input Validation

- [ ] 6 – Invalid `permissionLevel` returns 400
- [ ] 7 – `durationMinutes` above max TTL (e.g., 240) → 400
- [ ] 8 – `durationMinutes` <= 0 → 400
- [ ] 9 – Empty/null `targetDatabase` → 400
- [ ] 10 – Concurrent access requests → no conflicts
---

## 🔍 3. PostgreSQL – Role & Permissions Verification

- [ ] 16 – User exists in `pg_roles`
- [ ] 17 – Only granted allowed privileges (e.g. no DROP)
- [ ] 18 – User is removed after TTL
- [ ] 19 – `READ_ONLY` user cannot perform DELETE

---

## 📦 5. MongoDB – Audit Logging

- [ ] 20 – Audit entry saved with requestor, username, and TTL
- [ ] 21 – Audit entry includes permission level
- [ ] 22 – Audit entry does not store the password

### 🔹 Phase 3: Security & Authorization

- [ ] **Test**: only authorized users can request access (JWT)
- [ ] **Code**: implement Spring Security with JWT
- [ ] **Test**: approval is required before access is granted (simulated for PoC)
- [ ] **Code**: simulate access approval flow

### 🔹 Phase 4: Monitoring (Optional)

- [ ] Integrate Suricata (Docker container)
- [ ] Add simple rule: detect `SELECT *` without `WHERE`, `DROP`, etc.
- [ ] Log detected incidents

---

## 🧰 Tech Stack

- Java 21 + Groovy (Spock)
- Spring Boot 3
- Gradle
- PostgreSQL (target DB)
- MongoDB (audit logs)
- Docker + Docker Compose
- JWT (auth)
- Suricata (optional IDS)

---

## 🔐 PostgreSQL Permission Model – Explained

PostgreSQL uses **Role-Based Access Control (RBAC)** with granular privileges at various levels (database, schema, table, function).  
Full list in [PostgreSQL docs](https://www.postgresql.org/docs/current/ddl-priv.html)

### ❗ Why only a subset of permissions?

This system is designed for **safe and temporary developer access**.  
Granting the full range of PostgreSQL privileges (like `TRUNCATE`, `ALTER`, `DROP`, `EXECUTE`) would risk:

- accidental data loss or corruption,
- bypassing audit/approval flows,
- security violations.

We intentionally support a **narrow, safe subset of permissions**, mapped to abstract roles in the app.

 * Supported PostgreSQL versions: 12.x – 16.x
---

### ✅ Supported Permission Levels

```java
enum PermissionLevel {
    READ_ONLY,
    READ_WRITE,
    DELETE
}
```

| Permission Level | SQL Grants                                        | Notes                        |
|------------------|---------------------------------------------------|------------------------------|
| READ_ONLY        | CONNECT, USAGE, SELECT                            | For read-only diagnostics    |
| READ_WRITE       | CONNECT, USAGE, SELECT, INSERT, UPDATE            | For temporary fix/debugging  |
| DELETE           | + DELETE (in addition to READ_WRITE)              | Requires separate approval   |

> ✅ These correspond to real PostgreSQL privileges granted to a temporary user.

---

### 🧪 Example: Creating a Read-Only PostgreSQL Role

```sql
CREATE ROLE temp_read_user WITH LOGIN PASSWORD 'securepwd';
GRANT CONNECT ON DATABASE mydb TO temp_read_user;
GRANT USAGE ON SCHEMA public TO temp_read_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO temp_read_user;
```

---

## 🔌 Abstraction for Future Database Engines

To support other databases (e.g., Oracle, MySQL):

1. Define abstract permission levels in the app: `READ_ONLY`, `READ_WRITE`, `DELETE`, `ADMIN`, etc.
2. Implement `DatabaseAccessProvider` interface:
```java
interface DatabaseAccessProvider {
    void createTemporaryUser(String username, Duration ttl, PermissionLevel level);
    void revokeUser(String username);
}
```
3. Add implementations per engine:
   - `PostgresAccessProvider`
   - `OracleAccessProvider`
   - `MySQLAccessProvider`
4. Core logic must remain **DB-agnostic** and depend only on the interface.

---

## 🚀 Summary

This project is being developed as part of a postgraduate engineering program in Cybersecurity Engineering (2024/2025) at Warsaw University of Technology.

Its goal is to demonstrate a practical and secure solution to a common real-world problem: enabling temporary and auditable access to production databases in emergency or debugging scenarios. The solution emphasizes automation, granular permissions, time-bound access, and audit logging — all aligned with modern cybersecurity best practices.
