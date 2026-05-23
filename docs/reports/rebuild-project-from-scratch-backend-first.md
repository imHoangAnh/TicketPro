# Backend-First Rebuild Roadmap

## Purpose

This report explains how to rebuild the project as a clean backend-first event
ticket booking system.

The goal is not to keep extending the legacy ticket demo. Treat the current
repository as a Java/Spring multi-module shell and rebuild the product around
the accepted event ticket booking contract.

## Target Outcome

At the end of the backend-first rebuild, the backend should support:

- User registration and login.
- JWT access tokens.
- Refresh tokens stored in Redis only.
- USER and ADMIN roles.
- Public active event browsing.
- Admin event and ticket type management.
- Order placement by `ticketTypeId`.
- Redis Lua stock reservation.
- MySQL order/payment persistence.
- Pending order cancellation with stock restore.
- Mock payment first, VNPAY sandbox after the mock flow is stable.
- Local runtime with only MySQL and Redis as required infrastructure.

## Working Rule

Do not build everything at once. Implement one vertical backend slice at a
time, and after each slice run validation before moving on.

Recommended validation ladder:

```text
mvn test
mvn package
docker-compose -f environment/docker-compose-dev.yml config
```

Use temporary Java 21 if needed:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## Current Clean Model

Use these as the accepted root-package domain entities:

```text
com.xxxx.ddd.domain.model.entity.User
com.xxxx.ddd.domain.model.entity.Role
com.xxxx.ddd.domain.model.entity.UserRole
com.xxxx.ddd.domain.model.entity.Event
com.xxxx.ddd.domain.model.entity.TicketType
com.xxxx.ddd.domain.model.entity.Order
com.xxxx.ddd.domain.model.entity.OrderItem
com.xxxx.ddd.domain.model.entity.Payment
```

Refresh tokens are not part of the active entity model. Store refresh token
state in Redis through a `RefreshTokenStore` application contract. If DB-backed
session metadata is needed later, make that a separate architecture decision
and never store raw refresh tokens in MySQL.

## Legacy Code To Retire

Do not add new behavior to these legacy concepts:

```text
Ticket
TicketDetail
TickerOrder
PaymentTransaction
Booking
InventoryAllotDetail
InventoryBucketConfig
```

They can stay temporarily only when needed for compilation. As each new flow is
implemented, remove the old service/controller/repository path it replaces.

## Step 1: Freeze The Runtime Scope

Keep required local infrastructure to:

```text
MySQL
Redis
```

Do not reintroduce:

```text
Kafka
RabbitMQ
Prometheus
Grafana
ELK
Resilience4j demo endpoints
Employee demo login
API key demo endpoints
Monthly sharded order tables
Benchmark artifacts
```

Deliverables:

- `environment/docker-compose-dev.yml` contains only MySQL and Redis.
- `environment/mysql/init/ticket_init.sql` creates only accepted product
  tables.
- `docs/product/local-development.md` explains the backend runtime.

Validation:

```text
docker-compose -f environment/docker-compose-dev.yml config
mvn package
```

## Step 2: Rebuild The Schema From Zero

Start from a clean MySQL database and create only product tables:

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

Do not add `refresh_tokens` for the active scope. Refresh token state is
Redis-only.

Important constraints:

- `users.email` is unique.
- `roles.name` is unique.
- `user_roles(user_id, role_id)` is unique.
- `ticket_types.event_id` references `events.id`.
- `orders.user_id` references `users.id`.
- `order_items.order_id` references `orders.id`.
- `order_items.ticket_type_id` references `ticket_types.id`.
- `payments.order_id` references `orders.id`.
- `ticket_types.stock_available >= 0`.
- `ticket_types.price > 0`.

Recommended seed data:

- `ADMIN` and `USER` roles.
- One admin account.
- One user account.
- One active event.
- One inactive event.
- Two ticket types for the active event.

Deliverables:

- Clean SQL init script.
- Integration test that runs the SQL against H2 MySQL mode.
- Repository integration test for unique email and stock constraints.

Validation:

```text
mvn test
```

## Step 3: Stabilize Backend Module Boundaries

Keep the target dependency direction:

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
```

Responsibilities:

- `xxxx-domain`: entities, enums, domain services, repository interfaces.
- `xxxx-application`: use cases and transaction orchestration.
- `xxxx-infrastructure`: JPA adapters, Redis adapters, payment gateway
  adapters.
- `xxxx-controller`: REST DTOs, controllers, security entry points.
- `xxxx-start`: bootstrapping and configuration.

Avoid putting database, Redis, HTTP, or Spring Security implementation details
inside domain classes.

Deliverables:

- Domain repository interfaces for each aggregate.
- Infrastructure adapters implementing those interfaces.
- JPA mapper interfaces in infrastructure.

Validation:

```text
mvn package
```

## Step 4: Implement Auth And Authorization

Build auth before order/payment because later flows need `userId` and roles.

Recommended choice:

- Store access token as signed JWT.
- Store refresh token state in Redis.
- Keep DB `RefreshToken` out of the active product model.

API:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

Implementation pieces:

- `RegisterCommand`, `LoginCommand`, `RefreshCommand`.
- `AuthAppService`.
- `PasswordEncoder`.
- `JwtTokenProvider`.
- `RefreshTokenStore` interface.
- Redis implementation for refresh token store.
- Spring Security config.
- Role checks for `USER` and `ADMIN`.

Rules:

- Email must be unique.
- Password must be hashed.
- Login returns access token and refresh token.
- Refresh rotates or replaces refresh token.
- Logout revokes refresh token in Redis.
- `/api/auth/me` returns current user identity and roles.

Validation:

```text
mvn test
mvn package
```

Add tests for:

- Register success.
- Duplicate email rejection.
- Login success.
- Wrong password rejection.
- Refresh token revoke/expiry behavior.
- ADMIN endpoint rejects USER.

## Step 5: Implement Events And Ticket Types

Build event browsing and admin management before order placement.

Public API:

```text
GET /api/events
GET /api/events/{eventId}
```

Admin API:

```text
POST   /api/admin/events
PUT    /api/admin/events/{eventId}
DELETE /api/admin/events/{eventId}
PUT    /api/admin/events/{eventId}/active
PUT    /api/admin/events/{eventId}/inactive
POST   /api/admin/events/{eventId}/ticket-types
PUT    /api/admin/ticket-types/{ticketTypeId}
DELETE /api/admin/ticket-types/{ticketTypeId}
```

Rules:

- Public users see only active events.
- A ticket type belongs to one event.
- Ticket type price must be positive.
- Ticket type stock must be non-negative.
- An event with paid orders should not be deleted; mark it inactive.
- Admin changes should invalidate event/ticket cache if caching is added.

Deliverables:

- Event command/query DTOs.
- Event application service.
- Ticket type application service.
- Admin controller.
- Public controller.

Validation:

```text
mvn test
mvn package
```

Add tests for:

- Public list excludes inactive events.
- Admin can create/update event.
- Non-admin cannot access admin endpoints.
- Ticket type rejects invalid price/stock.

## Step 6: Implement Order And Redis Stock

Build this after auth and event APIs are stable.

User API:

```text
POST /api/orders
GET  /api/orders/my
GET  /api/orders/{orderId}
PUT  /api/orders/{orderId}/cancel
```

Order request:

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

Rules:

- Order APIs use `ticketTypeId`, not `ticketId`.
- User must be authenticated.
- Ticket type must exist and be active.
- Event must be active.
- Quantity must be positive.
- Redis Lua reserves stock atomically.
- MySQL stock is updated in the same application flow.
- If MySQL write fails after Redis decrement, compensate Redis or record a
  reconciliation item.
- Order starts as `PENDING`.
- Only pending orders can be cancelled.
- A user can cancel only their own pending order.
- Admin can inspect orders.

Recommended stock flow:

```text
1. Validate user and request.
2. Load ticket type and event.
3. Run Redis Lua decrement.
4. Start DB transaction.
5. Decrement MySQL ticket_types.stock_available conditionally.
6. Create order and order item.
7. Commit transaction.
8. If DB fails after Redis decrement, restore Redis or write reconciliation.
```

Deliverables:

- Redis Lua script.
- `StockReservationService`.
- `OrderAppService`.
- `OrderController`.
- `OrderRepository`.
- `OrderItemRepository`.
- Optional `stock_adjustment_log` if compensation cannot be guaranteed.

Validation:

```text
mvn test
mvn package
```

Add tests for:

- Successful order creates pending order and order item.
- Insufficient stock fails.
- Concurrent stock reservation cannot oversell.
- Cancel pending order restores stock.
- Paid order cannot be cancelled by normal user.

## Step 7: Implement Payment

Start with mock payment before VNPAY.

Mock API:

```text
POST /api/payments/{orderId}/mock-success
```

Rules:

- Order must belong to the current user.
- Order must be `PENDING`.
- Payment amount must match order total.
- Mock success marks payment `SUCCESS`.
- Mock success marks order `PAID`.

Then implement VNPAY:

```text
POST /api/payments/{orderId}/vnpay
```

Add callback handling only after the payment URL creation flow is tested.

Deliverables:

- `PaymentAppService`.
- `PaymentGateway` interface.
- `MockPaymentGateway`.
- `VnPayGateway`.
- Payment controller.

Validation:

```text
mvn test
mvn package
```

Add tests for:

- Mock payment success.
- Cannot pay another user's order.
- Cannot pay cancelled order.
- Duplicate successful payment is rejected or idempotent.
- VNPAY signature verification if callback is implemented.

## Step 8: Clean Legacy Backend Paths

After auth, events, orders, and payment are implemented on the new model,
remove replaced legacy paths.

Remove or retire:

- Legacy ticket controllers/services/repositories using `Ticket` and
  `TicketDetail`.
- Legacy order paths using `TickerOrder`.
- Legacy payment paths using `PaymentTransaction`.
- Legacy booking flow if `Order` fully replaces it.
- Any SQL/table references to `ticket`, `ticket_item`, or old order tables.

Do this only after the new equivalent endpoint exists and has tests.

Validation:

```text
rg -n "TicketDetail|TickerOrder|PaymentTransaction|ticket_item|ticket_order_|ticketId" xxxx-* environment docs
mvn test
mvn package
```

## Step 9: Add API Documentation

Once backend endpoints are stable, update:

```text
docs/product/api.md
howtostart.md
README.md or a product README
```

Include:

- Local startup steps.
- MySQL/Redis ports.
- Seed accounts.
- Auth flow.
- Example curl requests.
- Order/payment flow.
- Known limitations.

## Suggested Build Order

Use this order when working story by story:

| Order | Work | Result |
| --- | --- | --- |
| 1 | Runtime cleanup | MySQL + Redis only |
| 2 | Schema/domain model | Clean tables and root-package entities |
| 3 | Auth | Users can register/login/refresh/logout |
| 4 | Event APIs | Public browsing and admin management |
| 5 | Order + stock | Users can reserve stock and create pending orders |
| 6 | Mock payment | Users can pay pending orders locally |
| 7 | VNPAY sandbox | Payment provider demo flow |
| 8 | Legacy removal | Old demo paths gone |
| 9 | Docs and polish | Project is explainable and runnable |

## Stop Conditions

Stop and update docs before continuing if:

- A table name changes.
- An endpoint request/response shape changes.
- Refresh token storage changes from Redis-only to DB-backed or the reverse.
- Stock compensation needs a new table.
- Any legacy entity must remain longer than expected.
- Validation cannot prove a business rule.

## Recommended Next Story

The next backend story should be:

```text
E03 Auth and authorization
```

Suggested first slice:

```text
US-003 Email/password registration and login with USER/ADMIN roles and Redis refresh tokens
```

Keep Google OAuth2 out of the first auth slice. Add it after local auth and
role-based access control are tested.
