# dbaccess – Core Backend Module

This module is the core backend component of the **Safe Temporary Access to Databases** project.  
It is developed as part of a postgraduate engineering program in Cybersecurity Engineering (2024/2025) at Warsaw University of Technology.

Its purpose is to implement secure, temporary, and auditable access mechanisms to production databases for developers or ops engineers — especially in debugging or emergency scenarios.

---

## 🚀 Technologies

- **Java 21** + **Groovy 4**
- **Spring Boot 3.2**
- **Spock 2.4 (Groovy 4)** – for expressive testing
- **PostgreSQL** – main target database
- **Gradle** – with multi-module and integration test support
- **JUnit Platform** – used via Spock

---

## 🧪 Testing Strategy

This project is built in **TDD style** (Test-Driven Development).  
Unit and integration tests are clearly separated:

- Unit tests are located under:
  ```
  src/test/groovy
  ```

- Integration tests (Spring context bootstrapped) are located under:
  ```
  src/integrationTest/groovy
  ```

Tests use the Spock framework and run via JUnit Platform.

Run tests:
```bash
./gradlew test
./gradlew integrationTest
```

---

## 🧱 Project Structure

```
dbaccess/
├── src/
│   ├── main/               # Main backend application logic
│   ├── test/               # Unit tests
│   └── integrationTest/    # Spring Boot integration tests
├── build.gradle            # Project configuration (Groovy DSL)
├── integrationTest.gradle  # Logic for separate integrationTest sourceSet
└── README.md
```

---

## ▶️ Running the App

To run the app locally with a Spring profile (e.g. `test`):

```bash
./gradlew :dbaccess:bootRun --args='--spring.profiles.active=test'
```

Or define a custom run task (e.g. `runDev`) if needed.

---

## 🧠 Design Goals

- Time-limited, minimal-privilege credentials
- Audit logs for access tracking
- Extensible support for other databases (via `DatabaseAccessProvider` abstraction)
- Optional monitoring with Suricata

---

## 📝 Notes

- The module is not using Spring Data JPA or Hibernate — plain JDBC and custom queries are preferred for low-level access control.
- Future modules may provide orchestration, frontend, or monitoring integrations.

---

## 📚 License

This is an academic project used for research and demonstration purposes.
