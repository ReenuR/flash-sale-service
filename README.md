# Flash Sale Service
A high-performance Spring Boot microservice designed to handle flash sale events with
thousands of concurrent users. Solves two critical engineering problems:
**thundering herd** and **oversell**.

## The Problem

Flash sales create two dangerous scenarios:
1. **Thundering herd** — thousands of users hit Buy simultaneously, overwhelming the system
2. **Oversell** — two users buy the last item at the same time due to race conditions

## The Solution
User Request --> 

Rate Limiter (Redis Sliding Window) --> Too many requests? → 429

Inventory Check (Redis Atomic DECR) --> Sold out? → 409 

Save Order (PostgreSQL) --> Publish Event (Kafka) --> 200 OK

## Key Engineering Decisions

### 1. Sliding Window Rate Limiting
Chose sliding window over token bucket because flash sales demand strict fairness —
no user should be able to burst requests by saving up tokens before the sale starts.

Each user gets max 5 requests per 60 second window. Implemented using Redis Sorted Sets:
- Score = request timestamp
- Old entries automatically expire as window slides forward
- O(log N) operations — fast even under high load

### 2. Atomic Inventory with Redis DECR
Normal check-then-decrement pattern creates race conditions under high concurrency:
Thread 1: reads stock=1 ✓
Thread 2: reads stock=1 ✓  ← both think they can proceed
Thread 1: decrements → 0
Thread 2: decrements → -1  ← oversold

**Redis DECR is atomic — combines read + decrement + write in one operation.
No thread can interrupt it. 1000 concurrent requests become a clean queue.

### 3. Compensating Transaction
If DB save fails after inventory is reserved — we increment Redis back:**

Reserve inventory ✓
Save to DB ✗ → increment Redis back (undo)

Prevents inventory being permanently locked when order creation fails.

### 4. Centralized Redis Key Management
All Redis keys generated through `RedisKeys` utility class — prevents key mismatch
bugs across services and makes key format changes trivial.

## Tech Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.2.3 | Framework |
| Redis (Lettuce) | Rate limiting + atomic inventory |
| PostgreSQL | Order persistence |
| Kafka | Order event streaming |
| Docker | Local infrastructure |

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/flash-sale/{saleId}/init?quantity=N` | Initialize stock before sale |
| `POST` | `/api/flash-sale/{saleId}/buy` | Purchase item |
| `GET` | `/api/flash-sale/{saleId}/stock` | Check remaining stock |

### Buy Request Body
```json
{
  "userId": "user-123",
  "productId": "PS5"
}
```

### Response Codes
| Code | Meaning |
|---|---|
| 200 | Order placed successfully |
| 409 | Sold out |
| 429 | Rate limit exceeded |
| 500 | Unexpected error |

## Running Locally

### Prerequisites
- Java 17
- Docker Desktop

### Start Infrastructure
```bash
docker-compose up -d
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Test the Flow
```bash
# 1. Initialize stock
curl -X POST "http://localhost:8080/api/flash-sale/sale-001/init?quantity=100"

# 2. Buy item
curl -X POST "http://localhost:8080/api/flash-sale/sale-001/buy" \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-123", "productId": "PS5"}'

# 3. Check stock
curl "http://localhost:8080/api/flash-sale/sale-001/stock"
```

## Future Improvements
- JWT authentication for userId verification
- Redis fallback strategy if Redis goes down during sale
- Unit and integration tests
- Prometheus + Grafana monitoring
- Configurable rate limits per sale type
- Distributed tracing with Zipkin

## Related Projects
- [Payment Order Processing](https://github.com/ReenuR/payment-order-processing) —
  downstream service that processes orders created by this service
