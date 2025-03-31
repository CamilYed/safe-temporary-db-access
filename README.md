# Secure and Temporary Access to Production Databases

This project is a **proof of concept (PoC)** for building a secure, temporary access system for production databases, designed for developers and operations teams. It provides **time-limited**, **audited**, and **least-privilege** database access with optional network monitoring.

The core idea: let developers request short-lived database access through a secure API. Access is granted with minimal permissions, and then revoked automatically after a defined TTL.

---

## 💡 Key Principles

- **Security First**: time-limited credentials, no password reuse, audit logs.
- **TDD-Driven Development**: features are always implemented after writing readable, expressive tests.
- **Database-Agnostic Core**: PostgreSQL support first, with extensibility for other databases (e.g., Oracle, MySQL).

---

## 🧪 Test-Driven Development Workflow

All functionality in this project is developed using **TDD**. We follow this cycle:

1. Write a failing test (Spock Specification)
2. Implement the simplest code to make it pass
3. Refactor if needed

We aim for expressive and business-readable tests, following patterns like those described by Allegro:  
📖 [Readable Tests by Example](https://blog.allegro.tech/2022/02/readable-tests-by-example.html)

---

## ✅ TODO (TDD-first)

### 🔹 Phase 1: Project Setup

- [ ] Initialize Spring Boot + Gradle project
- [ ] Add basic Spock test support
- [ ] Set up Docker Compose (PostgreSQL + MongoDB)
- [ ] Add GitHub Actions CI (build + test)

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

## 🔐 PostgreSQL Permission Model – Summary

PostgreSQL uses **roles** (users or groups) and **privileges** that can be assigned to them:

### Common privileges:

| Privilege | Level     | Purpose                             |
|----------|------------|-------------------------------------|
| CONNECT  | Database   | Allow connection                    |
| USAGE    | Schema     | Allow access to schema and objects  |
| SELECT   | Table      | Read data                           |
| INSERT   | Table      | Add data                            |
| UPDATE   | Table      | Modify data                         |
| EXECUTE  | Function   | Call stored procedures              |

#### Example: Creating a read-only user
```sql
CREATE ROLE readonly_user WITH LOGIN PASSWORD 'securepwd';
GRANT CONNECT ON DATABASE mydb TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;
```

---

## 🔌 Abstraction for Future Database Engines

To support other databases (e.g., Oracle, MySQL):

1. Define abstract permission levels in the app: `READ_ONLY`, `WRITE`, `ADMIN`, etc.
2. Implement `DatabaseAccessProvider` interface:
```java
interface DatabaseAccessProvider {
    void createTemporaryUser(String username, Duration ttl, PermissionLevel level);
    void revokeUser(String username);
}
```
3. Add separate implementations:
   - `PostgresAccessProvider`
   - `OracleAccessProvider`
   - ...
4. Core logic should **only talk to the interface**, not the database-specific details.

---

## 🚀 Summary

This project addresses a real-world need in secure environments: giving developers safe access to production data only when necessary. Through a mix of automation, access control, and audit logging, we ensure a balance between agility and compliance.
