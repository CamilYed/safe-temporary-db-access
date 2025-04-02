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

### 🔹 Phase 2: Core Access Request Flow

1. [ ] **Test**: requesting access creates a user in PostgreSQL with correct permissions
2. [ ] **Code**: implement `/access-request` endpoint
3. [ ] **Test**: credentials are returned once and not persisted
4. [ ] **Code**: temporary credentials generation logic
5. [ ] **Test**: TTL expiration removes user from database
6. [ ] **Code**: scheduled task for user cleanup
7. [ ] **Test**: all access actions are logged in MongoDB
8. [ ] **Code**: implement Mongo audit logging

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
