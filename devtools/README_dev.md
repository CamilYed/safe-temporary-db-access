# 🚀 Local Development Guide: Safe Temporary DB Access

This guide helps you get started with the project locally **without requiring IntelliJ IDEA**.

---

## ✅ Prerequisites

Make sure you have the following installed:

- Docker + Docker Compose
- Python 3.10+
- `pip` (Python package installer)
- `openssl`

---

## 🐳 Step 1: Start Docker Infrastructure

We provide a Docker Compose setup that runs:

- PostgreSQL
- MongoDB
- The Java application (if built with Docker)

### 🔧 Run services

```bash
cd dbaccess/devtools/docker
docker-compose up --build -d
```

This will launch the database containers.

---

## 🔑 Step 2: Generate EC256 Key Pair for JWT

The application uses **EC (elliptic curve)** cryptography with **P-256 curve** for JWT.

Run the following to create keys:

```bash
# Generate private EC key
openssl ecparam -name prime256v1 -genkey -noout -out ec256-private.pem

# Extract public key in PEM
openssl ec -in ec256-private.pem -pubout -out ec256-public.pem

# Export public key in DER format (required by Spring Boot)
openssl ec -in ec256-private.pem -pubout -outform DER -out ec256-public.der
```

> ⚠️ Place `ec256-public.der` in a location accessible to the Spring app via `JwtKeyProperties`.

---

## 🐍 Step 3: Generate JWT Token with Python

We provide a script with a clickable menu for generating valid JWT tokens.

### ✅ Run the generator

```bash
cd dbaccess/devtools
python3 generate_token.py
```

The script allows:

- selecting one of allowed users (`alice`, `bob`, `charlie`)
- TTL max. **5 minutes**
- selecting your EC256 private key
- producing a valid JWT

### 📦 First time? Install dependencies:

```bash
pip install -r requirements.txt
```

---

## 📄 .env Configuration

Define the following in `.env` (already provided):

```dotenv
TEST1_DB_URL=jdbc:postgresql://localhost:5432/test1
TEST1_DB_USERNAME=admin
TEST1_DB_PASSWORD=admin

MONGO_URL=mongodb://localhost:27017
```

---

## 🚀 Running the Application

### 1. Gradle (local dev mode):

```bash
./gradlew bootRun
```

### 2. Docker image (production-like):

```bash
cd dbaccess
./gradlew clean build
cp build/libs/*.jar devtools/docker/app.jar
cd devtools/docker
docker-compose up --build -d
```

---

## 🔐 Authentication

Use the generated JWT token and include it in requests:

```http
Authorization: Bearer <your_token>
```

Only subjects in `example-users.yaml` allowlist will be accepted:

```yaml
allowlist:
  - alice
  - bob
  - charlie
```

---

## 🧪 Swagger UI

If enabled (optional dev profile), visit:

```
http://localhost:8080/swagger-ui.html
```

Use your generated JWT as a bearer token to authorize.

---

## 📦 Optional Enhancements

- Add Nginx reverse proxy with HTTPS termination
- Mount volume for JWT keys for production deploy
- Deploy image to GitHub Container Registry