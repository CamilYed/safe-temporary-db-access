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

### ✅  Phase 1: Project Setup

- [✅] Initialize Spring Boot + Gradle project
- [✅] Add Spock testing support
- [✅] Write integration test that loads Spring context and asserts it is not null
- [✅] Configure minimal CI (GitHub Actions)

### ✅ Phase 2 – Integration Test Strategy Checklist

#### 🔹 Step 2. Functional Core Scenarios (Happy Path)

- [✅] Valid request creates user with `READ_ONLY` permissions
- [✅] User with `READ_WRITE` gets INSERT, UPDATE rights
- [✅] User with `DELETE` gets DELETE right
- [✅] TTL expiration removes user from DB
- [✅] Response includes username/password only once
- [✅] Username/password follow secure formats and are validated
- [✅] Scheduled revocation logic removes expired access
- [✅] Unsafe inputs (e.g., SQLi) are rejected and logged
- [✅] All access and revocation events are written to audit log

---

### ✅  Step 3. PostgreSQL – Role & Permissions Verification

- [✅] User exists in `pg_roles`
- [✅] Only granted allowed privileges (e.g. no DROP)
- [✅] User is removed after TTL
- [✅] `READ_ONLY` user cannot perform DELETE

---

### ✅  Step 4. MongoDB – Audit Logging

- [✅] Audit entry saved with requestor, username, and TTL
- [✅] Audit entry includes permission level
- [✅] Audit entry does not store the password
- [✅] Audit log retains failed revocation entries (e.g. invalid user/DB)

---

### 🔐 Phase 3: Security & Monitoring

- [✅] Static Application Security Testing (SAST) via SonarCloud
- [✅] Test coverage >80% (measured by SonarCloud)
- [ ] [Optional] Integrate penetration testing tool (e.g. OWASP ZAP via GitHub Action)

---

## [TODO] 🐳 Running Locally with Docker Compose

1. **Generate EC keys** for JWT (using OpenSSL):

```bash
# Generate private key
openssl ecparam -name prime256v1 -genkey -noout -out ec256-private.pem

# Extract public key (in PEM)
openssl ec -in ec256-private.pem -pubout -out ec256-public.pem

# Convert public key to DER format (required by the app)
openssl ec -in ec256-private.pem -pubout -outform DER -out ec256-public.der
```

2. **Set environment variables** (or `.env` file):

```env
TEST1_DB_URL=jdbc:postgresql://localhost:5432/test
TEST1_DB_USERNAME=admin
TEST1_DB_PASSWORD=admin
```

3. **Start with Docker Compose** (MongoDB + PostgreSQL):

```bash
docker-compose up -d
./gradlew bootRun
```

4. **Login with a token**: JWT subject must be one of the users listed in `example-users.yaml`:

```yaml
allowlist:
  - alice
  - bob
  - charlie
```

---

## ✅ Static Configuration

### example-users.yaml

```yaml
allowlist:
  - alice
  - bob
  - charlie
```

### db-access.yaml

```yaml
db:
  databases:
    test1:
      envPrefix: TEST1
    test2:
      envPrefix: TEST2
```

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
