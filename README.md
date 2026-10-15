# 🔗 URL Shortener

A full-stack URL shortener built with **Spring Boot**, **PostgreSQL**, **Redis**, and **React (Vite)**. It turns long links into short Base62 codes, redirects visitors quickly using a Redis cache, protects the create endpoint with IP-based rate limiting, and tracks click counts without hitting the database on every redirect.

---

## ✨ Features

- **Shorten any http/https URL** – generates a compact Base62 short code from the database ID
- **Fast redirects** – looks up the original URL in Redis first (15-minute TTL), falls back to PostgreSQL on a cache miss
- **HTTP 307 temporary redirect** – so every visit still passes through the service and can be counted
- **Write-behind click tracking** – clicks are incremented in Redis and flushed to PostgreSQL in batches by a scheduled job
- **Rate limiting** – max 10 shorten requests per minute per client IP, backed by Redis (returns `429`)
- **Input validation** – rejects empty, malformed, non-http(s) and over-long (>2048 chars) URLs
- **Centralized error handling** – `@RestControllerAdvice` maps domain exceptions to proper HTTP statuses
- **React frontend** – clean UI with client-side validation, loading state, and one-click copy
- **Load testing** – included [k6](https://k6.io/) script that ramps up to 1000 virtual users
- **Dockerized infrastructure** – PostgreSQL and Redis start with a single `docker compose` command

---

## 🛠️ Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Spring Data Redis) |
| Database | PostgreSQL 18 |
| Cache / Counters / Rate limiter | Redis 7 |
| Encoding | [`io.seruco.encoding:base62`](https://github.com/seruco/base62) 0.1.3 |
| Frontend | React 19, Vite, React Router 7, Axios |
| Build tools | Maven (wrapper included), npm |
| Utilities | Lombok |
| Load testing | k6 |

---

## 🏗️ Architecture

```
                 ┌──────────────────────┐
                 │  React + Vite (5173) │
                 └──────────┬───────────┘
                            │ POST /url
                            ▼
┌────────────────────────────────────────────────────────┐
│                  Spring Boot API (8080)                │
│                                                        │
│  RateLimitInterceptor ──► Redis  (rate:<ip>)           │
│  UrlController                                         │
│     ├─ POST /url ──► UrlShortnerServise ──► PostgreSQL │
│     └─ GET /{code} ─► Redis (url:<code>) ─► PostgreSQL │
│                    └► Redis INCR (clicks:<code>)        │
│                                                        │
│  Schedular (every ~8 min)                              │
│     └─ Redis clicks:* ──► UPDATE url SET click_count   │
└────────────────────────────────────────────────────────┘
```

### How it works

**1. Creating a short URL** (`POST /url`)

1. `RateLimitInterceptor` increments `rate:<client-ip>` in Redis. The first hit sets a 60-second expiry; more than 10 hits in the window returns `429 Too Many Requests`.
2. The URL is validated (non-blank, ≤ 2048 chars, `http`/`https` scheme, has a host).
3. The URL is saved to PostgreSQL, which generates an auto-increment `id`.
4. The `id` is Base62-encoded to produce the short code, and the full short link is returned.

**2. Redirecting** (`GET /{shortCode}`)

1. Check Redis key `url:<shortCode>`. On a hit, use it.
2. On a miss, Base62-decode the code to the `id`, load the row from PostgreSQL, and cache the result in Redis for 15 minutes.
3. Increment `clicks:<shortCode>` in Redis.
4. Respond with `307 Temporary Redirect` and a `Location` header.

**3. Syncing click counts** (scheduled job)

Every ~500 seconds the scheduler scans Redis for `clicks:*` keys and, for each one:

- Decodes the key to a database ID (bad keys are deleted so they can't loop forever)
- Atomically reads and deletes the counter (`GETDEL`)
- Adds the count to `click_count` in PostgreSQL with a single `UPDATE ... SET click_count = COALESCE(click_count, 0) + :count`
- If the database write fails, the clicks are put back into Redis so nothing is lost

---

## 📁 Project Structure

```
url-shortener/
├── Docker-compose.yaml            # PostgreSQL + Redis
├── load-test.js                   # k6 load test
├── pom.xml
├── mvnw / mvnw.cmd                # Maven wrapper
├── src/
│   ├── main/java/com/UrlShortner/
│   │   ├── Application.java
│   │   ├── Dto/UrlReq.java                    # request body record
│   │   ├── config/
│   │   │   ├── AppConfig.java                 # Base62 bean
│   │   │   └── WebConfig.java                 # registers rate-limit interceptor
│   │   ├── controller/UrlController.java      # REST endpoints
│   │   ├── exceptions/                        # custom exceptions + global handler
│   │   ├── interceptors/RateLimitInterceptor.java
│   │   ├── model/Url.java                     # JPA entity
│   │   ├── repo/UrlRepo.java                  # JPA repository + click update query
│   │   ├── schedular/Schedular.java           # Redis → DB click flush
│   │   └── services/
│   │       ├── UrlShortnerServise.java        # shorten + resolve logic
│   │       ├── UrlClickTrackingService.java   # Redis click counter
│   │       └── RateLimiterService.java        # fixed-window rate limiter
│   ├── main/resources/application.properties
│   └── test/java/com/UrlShortner/ApplicationTests.java
└── urlShortnerfrontend/           # React + Vite app
    ├── src/
    │   ├── api/api.js             # Axios instance
    │   ├── components/UrlShortner.jsx / .css
    │   ├── App.jsx
    │   └── main.jsx
    └── package.json
```

---

## ✅ Prerequisites

- **Java 21+**
- **Node.js 18+** and npm
- **Docker** and Docker Compose (for PostgreSQL and Redis)
- *(Optional)* [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) for load testing

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd url-shortener
```

### 2. Start PostgreSQL and Redis

```bash
docker compose -f Docker-compose.yaml up -d
```

This starts:

| Service | Container | Port | Credentials |
| --- | --- | --- | --- |
| PostgreSQL 18 | `urlshortener-postgres` | `5432` | db `urlshortener`, user `postgres`, password `postgres123` |
| Redis 7 | `urlshortener-redis` | `6379` | none |

### 3. Run the backend

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**. Hibernate creates the `url` table automatically (`ddl-auto=update`).

### 4. Run the frontend

```bash
cd urlShortnerfrontend
npm install
npm run dev
```

Open **http://localhost:5173**, paste a long URL, and click **Shorten URL**.

---

## ⚙️ Configuration

All backend settings live in `src/main/resources/application.properties`:

| Property | Default | Description |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/urlshortener` | PostgreSQL JDBC URL |
| `spring.datasource.username` | `postgres` | Database user |
| `spring.datasource.password` | `postgres123` | Database password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-create/update schema |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |
| `logging.file.name` | `logs/app.log` | Log file location |
| `app.cors.allowed-origins` | `http://localhost:5173,http://localhost:3000` | Allowed frontend origins |

Values hard-coded in the source (change them in code if needed):

| Setting | Value | Location |
| --- | --- | --- |
| Rate limit | 10 requests / 60 s per IP | `RateLimiterService` |
| Redirect cache TTL | 15 minutes | `UrlShortnerServise` |
| Click flush interval | `fixedRate = 500000` ms (~8.3 min) | `Schedular` |
| Max URL length | 2048 characters | `UrlShortnerServise` |
| Short link base | `http://localhost:8080/` | `UrlShortnerServise` |
| Frontend API base URL | `http://localhost:8080` | `urlShortnerfrontend/src/api/api.js` |

> **Tip:** if the backend fails to start with `password authentication failed for user "postgres"`, a different PostgreSQL instance (or an old Docker volume created with another password) is likely answering on port 5432. Stop it or reset the volume with `docker compose down -v`.

---

## 📡 API Reference

### Create a short URL

```
POST /url
Content-Type: application/json
```

**Request body**

```json
{ "url": "https://example.com/a/very/long/link" }
```

**Response** `200 OK` (plain text)

```
http://localhost:8080/MTI=
```

**Errors**

| Status | When |
| --- | --- |
| `400 Bad Request` | URL is missing, blank, malformed, longer than 2048 chars, or not http/https |
| `429 Too Many Requests` | More than 10 requests per minute from the same IP |

**Example**

```bash
curl -X POST http://localhost:8080/url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://github.com"}'
```

### Redirect to the original URL

```
GET /{shortCode}
```

**Response** `307 Temporary Redirect` with a `Location` header pointing to the original URL.

**Errors**

| Status | When |
| --- | --- |
| `404 Not Found` | The short code is invalid or doesn't exist |

**Example**

```bash
curl -i http://localhost:8080/MTI=
```

---

## 🗄️ Data Model

**Table `url`**

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key, auto-generated (identity) |
| `url` | `VARCHAR` | Original long URL |
| `click_count` | `BIGINT` | Total clicks, updated in batches by the scheduler |

**Redis keys**

| Key | Purpose | TTL |
| --- | --- | --- |
| `url:<shortCode>` | Cached original URL | 15 minutes |
| `clicks:<shortCode>` | Pending click count awaiting flush | until flushed |
| `rate:<ip>` | Request counter for rate limiting | 60 seconds |

---

## 🧪 Testing

### Unit / integration tests

```bash
./mvnw test
```

Surefire is configured with `-Duser.timezone=UTC` to avoid timezone issues during tests.

### Load testing with k6

First create at least one short link and note its code (the script currently requests `/p`, so replace it with a real code from your database). Then run:

```bash
k6 run load-test.js
```

The test ramps from 0 → 100 → 250 → 500 → 1000 virtual users over about 3 minutes and asserts:

- `http_req_failed` rate **< 1%**
- `http_req_duration` p(95) **< 500 ms**
- every response is a `307` redirect (redirects are not followed)

---

## 🧭 Design Decisions

- **Base62 of the database ID** – no collision checks or random-code retries; every short code is unique by construction.
- **307 instead of 301** – browsers don't permanently cache the redirect, so each visit reaches the server and clicks are counted.
- **Redis for hot paths** – redirects, click counters, and rate limiting never wait on PostgreSQL in the common case.
- **Write-behind click counting** – thousands of clicks collapse into one `UPDATE` per short link per flush, which keeps database load low at the cost of slightly delayed counts.
- **Atomic `GETDEL` + restore on failure** – avoids double-counting and avoids losing clicks if the database is down during a flush.

---

## 🔮 Possible Improvements

- Move the short-link base URL (`http://localhost:8080/`) and the frontend API URL into environment-specific configuration
- Read secrets (DB password) from environment variables instead of `application.properties`
- Add custom aliases and link expiration
- Add an endpoint to view click statistics for a link
- Return JSON error bodies consistently so the frontend can show server messages
- Use `X-Forwarded-For` for client IP detection when running behind a proxy or load balancer
- Add unit and integration tests for the service layer and controllers
- Containerize the backend and frontend and add them to Docker Compose

---

## 📄 License

No license has been specified yet. Add a `LICENSE` file (for example MIT) to define how others may use this project.
