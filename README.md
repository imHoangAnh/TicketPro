# Event Ticket Booking System

An event ticket booking system with a DDD-flavoured multi-module Java/Spring
Boot backend and a React/Vite frontend. The application supports public event
browsing, ticket type selection, authenticated order placement, Redis-backed
stock reservation, mock payment, VNPAY sandbox payment, and an admin dashboard.

## Features

- Email/password registration and login.
- JWT access tokens with opaque refresh tokens stored in Redis.
- `USER` and `ADMIN` role-based access control.
- Public browsing for active events and active ticket types.
- Authenticated users can place orders, view their orders, and cancel pending
  orders.
- Redis Lua stock gate before MySQL stock update.
- Mock success payment and VNPAY sandbox payment.
- Admin event and ticket type management.
- Admin order visibility.
- React frontend with public pages, auth pages, cart/payment flow, and admin
  dashboard.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.5, Maven |
| Frontend | React, Vite, Axios, React Router, Lucide icons |
| Database | MySQL 8 |
| Cache / Session / Stock | Redis |
| Security | Spring Security, JWT, BCrypt |
| Payment | Mock payment, VNPAY sandbox |
| Local runtime | Docker Compose |

## Architecture

The backend is split into DDD-flavoured clean architecture modules:

```text
xxxx-start
  -> xxxx-controller
  -> xxxx-infrastructure

xxxx-controller
  -> xxxx-application

xxxx-application
  -> xxxx-domain

xxxx-infrastructure
  -> xxxx-domain
  -> xxxx-application contracts when needed
```

Module responsibilities:

- `xxxx-start`: Spring Boot entrypoint, runtime composition root, application
  configuration.
- `xxxx-controller`: REST controllers, security filter, web configuration, and
  request/response DTOs.
- `xxxx-application`: use case orchestration, transaction boundaries, DTOs,
  authentication, order, payment, cache, and port contracts.
- `xxxx-domain`: domain entities, enums, domain services, repository contracts,
  and business rules.
- `xxxx-infrastructure`: JPA adapters, Redis adapter, Redisson lock adapter,
  and VNPAY gateway adapter.
- `xxxx.fe.com`: React/Vite frontend.

Runtime flow:

```text
React frontend
  -> REST controller
      -> application use case
          -> domain service / repository contract
              -> infrastructure adapter
                  -> MySQL, Redis, VNPAY
```

## Project Structure

```text
.
|-- xxxx-start/             # Spring Boot main app and application.yml
|-- xxxx-controller/        # REST API, security, web config
|-- xxxx-application/       # Use cases, DTOs, ports
|-- xxxx-domain/            # Domain model and contracts
|-- xxxx-infrastructure/    # MySQL/Redis/VNPAY adapters
|-- xxxx.fe.com/            # React/Vite frontend
|-- environment/            # Docker Compose and MySQL init SQL
`-- docs/                   # Architecture, product contracts, story packets
```

## Prerequisites

Install:

- Docker Desktop.
- Java 21.
- Maven.
- Node.js 20+ or Node.js 22 LTS.
- npm.

Check your environment:

```powershell
docker --version
docker compose version
java -version
mvn -version
node -v
npm -v
```

If you use the bundled JDK path from this machine:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## Run MySQL and Redis with Docker

From the repository root:

```powershell
cd E:\JavaProject\ticketbook\xxxx.com-13-05-26
docker-compose -f environment\docker-compose-dev.yml up -d
```

Local connection details:

| Service | Host | Port | Username | Password | Database |
| --- | --- | --- | --- | --- | --- |
| MySQL | `localhost` | `3316` | `root` | `root1234` | `vetautet` |
| Redis | `127.0.0.1` | `6319` | none | none | n/a |

Check containers:

```powershell
docker ps
docker logs pre-event-mysql
docker logs pre-event-redis
```

The MySQL initialization script is:

```text
environment/mysql/init/ticket_init.sql
```

MySQL only runs init scripts when its data directory is empty. To reset the
local database:

```powershell
docker-compose -f environment\docker-compose-dev.yml down
Rename-Item environment\data\db_data environment\data\db_data_backup
docker-compose -f environment\docker-compose-dev.yml up -d
```

This switches the app to a fresh local database. The old data remains in the
renamed backup folder but is no longer used by the container.

## Connect with DataGrip

Create a MySQL data source:

```text
Host: localhost
Port: 3316
User: root
Password: root1234
Database: vetautet
```

JDBC URL:

```text
jdbc:mysql://localhost:3316/vetautet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
```

After connecting, open `Databases...`, select the `vetautet` schema, then open:

```text
vetautet -> tables
```

Main seeded tables:

```text
users
roles
user_roles
events
ticket_types
orders
order_items
payments
```

## Run the Backend

The backend runs on port `1122`.

Build the executable jar:

```powershell
cd E:\JavaProject\ticketbook\xxxx.com-13-05-26

$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

mvn -pl xxxx-start -am clean package
```

Run the jar:

```powershell
java -jar xxxx-start\target\xxxx-start-1.0-SNAPSHOT.jar
```

The backend has started successfully when the log contains:

```text
Tomcat started on port 1122
Started StartApplication
```

Check the public API:

```powershell
Invoke-RestMethod http://localhost:1122/api/events
```

Or open:

```text
http://localhost:1122/api/events
```

If you prefer Spring Boot Maven Plugin, use the full plugin coordinate:

```powershell
mvn -pl xxxx-start -am org.springframework.boot:spring-boot-maven-plugin:3.3.5:run
```

## Run the Frontend

Open a second terminal:

```powershell
cd E:\JavaProject\ticketbook\xxxx.com-13-05-26\xxxx.fe.com
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

The frontend calls the backend at:

```text
http://localhost:1122
```

Backend CORS allows:

```text
http://localhost:5173
```

Use `localhost`, not `127.0.0.1`, when opening the frontend.

Build the frontend:

```powershell
cd E:\JavaProject\ticketbook\xxxx.com-13-05-26\xxxx.fe.com
npm run build
```

## Test Accounts

If the database was initialized from `ticket_init.sql`, these accounts are
available:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@example.com` | `password123` |
| User | `user@example.com` | `password123` |

Recommended user flow:

1. Log in as `user@example.com`.
2. Open `/tickets`.
3. Select an event.
4. Select a ticket type and quantity.
5. Click the booking button to navigate to `/cart`.
6. Confirm order placement.
7. Select mock payment or VNPAY sandbox payment.

Note: opening `/cart` directly will not call the order API because the page
expects route state from the event detail page. Navigate through the ticket
detail page first.

## Main API Endpoints

Public:

```text
GET /api/events
GET /api/events/{eventId}
```

Auth:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

User:

```text
POST /api/orders
GET  /api/orders/my
GET  /api/orders/{orderId}
PUT  /api/orders/{orderId}/cancel
POST /api/payments/{orderId}/mock-success
POST /api/payments/{orderId}/vnpay
```

Admin:

```text
POST   /api/admin/events
PUT    /api/admin/events/{eventId}
DELETE /api/admin/events/{eventId}
PUT    /api/admin/events/{eventId}/active
PUT    /api/admin/events/{eventId}/inactive

POST   /api/admin/events/{eventId}/ticket-types
PUT    /api/admin/ticket-types/{ticketTypeId}
DELETE /api/admin/ticket-types/{ticketTypeId}

GET    /api/admin/orders
GET    /api/admin/orders/{orderId}
```

Create order request:

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

## Configuration

Backend configuration:

```text
xxxx-start/src/main/resources/application.yml
```

Important defaults:

```yaml
server:
  port: 1122

spring:
  datasource:
    url: jdbc:mysql://localhost:3316/vetautet
    username: root
    password: root1234
  data:
    redis:
      host: 127.0.0.1
      port: 6319
```

Environment variable overrides:

```text
AUTH_JWT_SECRET
AUTH_ACCESS_TOKEN_TTL_MINUTES
AUTH_REFRESH_TOKEN_TTL_DAYS
AUTH_REFRESH_COOKIE_SECURE
VNPAY_TMN_CODE
VNPAY_SECRET_KEY
VNPAY_PAY_URL
VNPAY_RETURN_URL
```

## Validation

Backend:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

mvn test
mvn package
```

Frontend:

```powershell
cd xxxx.fe.com
npm run build
```

Docker Compose config:

```powershell
docker-compose -f environment\docker-compose-dev.yml config
```

## Troubleshooting

### Maven reports `No plugin found for prefix spring-boot`

Use the full plugin coordinate:

```powershell
mvn -pl xxxx-start -am org.springframework.boot:spring-boot-maven-plugin:3.3.5:run
```

Or build and run the jar:

```powershell
mvn -pl xxxx-start -am clean package
java -jar xxxx-start\target\xxxx-start-1.0-SNAPSHOT.jar
```

### Backend cannot connect to MySQL

Check the container and port mapping:

```powershell
docker ps
docker logs pre-event-mysql
```

MySQL must expose:

```text
3316 -> 3306
```

### Backend cannot connect to Redis

Check Redis:

```powershell
docker ps
docker logs pre-event-redis
```

Redis must expose:

```text
6319 -> 6379
```

### Frontend has CORS or API call failures

- Backend must be running at `http://localhost:1122`.
- Frontend should be opened at `http://localhost:5173`.
- Do not open the frontend with `127.0.0.1:5173`.
- Log in before calling `/api/orders` or `/api/payments`.

### Opening `/cart` directly does not call the API

This is current behavior. `/cart` needs route state from the event detail page.
Use this flow:

```text
/tickets -> /ticket/{id} -> booking button -> /cart
```

## Project Documentation

- `docs/ARCHITECTURE.md`: architecture and module boundaries.
- `docs/product/`: current product contracts.
- `docs/stories/`: story packets grouped by epic.
- `docs/TEST_MATRIX.md`: behavior-to-validation evidence matrix.
- `docs/decisions/`: durable product and architecture decisions.

## Current Status

The main backend stories have validation evidence: runtime cleanup, domain data
model, auth, event/ticket management, order/stock, payment, and DDD boundary
refactor. The React frontend is implemented around the current `/api/*`
contracts and continues to be stabilized alongside the backend API.
