# Digital Wallet Backend

A production-grade digital wallet and payment backend system built with Java Spring Boot. This project demonstrates real-world backend engineering concepts including JWT authentication, payment gateway simulation, transaction safety, Redis caching, and Docker deployment.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3.5 |
| Security | Spring Security + JWT + Redis Token Blacklist |
| Database | PostgreSQL 18 |
| Cache | Redis 7 |
| ORM | Hibernate / JPA |
| Documentation | Swagger / OpenAPI |
| Deployment | Docker + Docker Compose + Nginx |
| Rate Limiting | Bucket4j |

---

## Architecture

```
Client → Nginx (port 80) → Spring Boot App (port 9090) → PostgreSQL / Redis
```

**Layered Architecture:**
```
Controller → Service → ServiceImpl → Repository → Database
```

---

## Key Features

### Authentication & Security
- JWT-based stateless authentication
- Refresh token flow with 7-day expiry
- Token blacklisting on logout using Redis
- Role-based access control (ROLE_USER, ROLE_ADMIN)
- Rate limiting (5 requests/min per IP)
- BCrypt password hashing

### Wallet Operations
- Create wallet on user registration
- Deposit, withdraw, transfer money
- Real-time balance check
- Paginated transaction history

### Transaction Safety
- **Pessimistic locking** — prevents race conditions on concurrent transfers using `SELECT FOR UPDATE`
- **Ordered locking** — prevents deadlocks by always locking wallets in consistent order
- **Idempotency keys** — prevents duplicate transactions on network retries
- **@Transactional** — ensures atomicity on all write operations

### Payment Gateway Simulator
- Create payment orders (PENDING status)
- Simulate payment processing
- Webhook callback handling with **HMAC-SHA256 signature verification**
- Payment order lifecycle: PENDING → SUCCESS
- Transaction record linked to payment order

### Observability
- Logback with rolling file appender (async, 10MB/file, 30-day retention)
- Correlation ID tracing on every request via `X-Correlation-ID` header
- Audit logs with IP address tracking for all key operations
- Profile-based logging (DEBUG for local, INFO for prod)

---

## Database Schema

```
users
  └── wallets (1:1)
       └── transactions (sender/receiver wallet FK)
            └── payment_orders (FK to transaction)
  └── refresh_tokens (1:N)

audit_logs (standalone)
```

---

## API Endpoints

### User
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/user` | No | Register user |
| POST | `/api/v1/user/login` | No | Login, returns JWT + refresh token |
| POST | `/api/v1/user/refresh` | No | Get new access token |
| POST | `/api/v1/user/logout` | Yes | Logout, blacklist JWT |

### Wallet
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/wallets` | Yes | Get wallet balance |
| POST | `/api/v1/wallets/deposit` | Yes | Deposit money |
| POST | `/api/v1/wallets/withdraw` | Yes | Withdraw money |
| POST | `/api/v1/wallets/transfer` | Yes | Transfer to another user |
| GET | `/api/v1/wallets/transactions` | Yes | Paginated transaction history |

### Payments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/payments` | Yes | Create payment order |
| POST | `/api/v1/payments/simulate/{orderId}` | Yes | Simulate payment |
| POST | `/api/v1/payments/webhook` | No | Webhook callback |

---

## Running Locally

### Prerequisites
- Java 21
- PostgreSQL 18
- Redis 7
- Maven 3.9+

### Steps

**1. Clone the repository:**
```bash
git clone https://github.com/AnkitSrinivas/digital-wallet-backend.git
cd digital-wallet-backend
```

**2. Create PostgreSQL database:**
```sql
CREATE DATABASE wallet_db;
```

**3. Create `application-local.properties`** in `src/main/resources/`:
```properties
server.port=9090

spring.datasource.url=jdbc:postgresql://localhost:5432/wallet_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=your_jwt_secret_base64
jwt.expiration=86400000

payment.webhook.secret=your_webhook_secret
refresh.token.expiration=604800000

spring.data.redis.host=localhost
spring.data.redis.port=6379

springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

logging.file.path=./logs
```

**4. Run the application:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**5. Access Swagger UI:**
```
http://localhost:9090/swagger-ui.html
```

---

## Running with Docker

### Prerequisites
- Docker Desktop
- Docker Compose

### Steps

**1. Clone the repository:**
```bash
git clone https://github.com/your-username/digital-wallet-backend.git
cd digital-wallet-backend
```

**2. Start all services:**
```bash
docker-compose up --build
```

This starts:
- PostgreSQL on port `5432`
- Redis on port `6379`
- Spring Boot app on port `9090`
- Nginx reverse proxy on port `80`

**3. Access the API:**
```
http://localhost/api/v1/user
http://localhost/swagger-ui.html
```

**4. Stop all services:**
```bash
docker-compose down
```

---

## Environment Variables (Docker)

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `POSTGRES_DB` | Database name | `wallet_db` |
| `POSTGRES_USER` | Database user | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `admin` |

---

## Key Concepts Demonstrated

### Pessimistic Locking
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM Wallet w WHERE w.user.userName = :userName")
Optional<Wallet> findWalletForUpdate(@Param("userName") String userName);
```
Prevents two concurrent requests from reading stale balance before writing.

### Idempotency
Every deposit, withdraw, and transfer requires a unique `Idempotency-Key` header. Duplicate requests with the same key return the previous result without re-processing.

### HMAC Webhook Verification
```
signature = HMAC-SHA256(orderId + amount, webhookSecret)
```
The simulator generates a signature and the webhook handler recalculates and compares it — preventing forged webhook requests.

### Redis Token Blacklist
On logout, the JWT is stored in Redis with TTL equal to its remaining expiry time. Every subsequent request checks the blacklist before authenticating.

---

## Project Structure

```
src/main/java/com/walletapp/
├── config/          # JWT, Security, Redis, Swagger config
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions
├── filter/          # JWT auth filter, Trace filter
├── repository/      # Spring Data JPA repositories
├── service/         # Service interfaces
│   └── impl/        # Service implementations
└── utility/         # IP utils and helpers
```

---

## API Response Format

All responses follow a consistent format:
```json
{
  "message": "Login Successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "abc123...",
    "userName": "john"
  },
  "status": 200
}
```

---

## License

This project is for educational and portfolio purposes.