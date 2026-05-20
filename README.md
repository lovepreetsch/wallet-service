# Wallet Management Service

This is a simple Wallet Management REST API project built using Spring Boot and PostgreSQL.

The project supports:

* Deposit money
* Withdraw money
* Check wallet balance

Technologies used:

* Java 17
* Spring Boot
* PostgreSQL
* Liquibase
* Docker
* JUnit

Liquibase is used for database migration and Docker Compose is used to run both application and database together.

To avoid balance issues during multiple requests, pessimistic locking is implemented while updating wallet balance.

---
Environment Variables

Application and database configurations can be changed without rebuilding containers.

Example .env file:

DB_NAME=wallet_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/wallet_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
---

# Database

Liquibase automatically creates the wallet table during application startup.

Sample wallets added for testing:

1. wallet id: `11111111-1111-1111-1111-111111111111`  
   balance: `5000.00`
2. wallet id: `22222222-2222-2222-2222-222222222222`  
   balance: `1000.00`

---

# API Endpoints

## Deposit / Withdraw

```http
POST /api/v1/wallet
```

Request Body:

```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

Response:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "balance": 6000.00
}
```

---

## Get Wallet Balance

```http
GET /api/v1/wallets/{walletId}
```

Response:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "balance": 6000.00
}
```

---

# Error Handling

Application handles:

* Wallet not found
* Invalid request
* Insufficient balance
* Invalid JSON

Example:

```json
{
    "error": "Not Found",
    "message": "Wallet not found with ID: 11111111-1111-1111-1111-111111111311",
    "timestamp": "2026-05-20T15:16:17.669058739",
    "status": 404
}
```

---

# Run Application

Run using Docker Compose:

```bash
docker-compose up --build
```

Application runs on:

```
http://localhost:8080
```

---

# Run Tests

```bash
mvn clean test
```
