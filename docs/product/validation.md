# Validation

## Backend Priority Tests

Auth:

- Register success.
- Login success.
- Login failure.
- Refresh token rotation.
- Logout invalidates refresh token.

Order:

- Place order success.
- Out of stock.
- Redis cache miss warmup.
- DB stock conflict triggers Redis rollback.
- Cancel pending order restores stock.

Payment:

- Mock payment marks order `PAID`.
- VNPAY callback success path.
- VNPAY callback failure path.

Security:

- `USER` cannot access admin APIs.
- Unauthenticated user cannot create order.

## Suggested Tools

- `spring-boot-starter-test`
- `spring-security-test`
- Testcontainers MySQL.
- Embedded Redis or Testcontainers Redis.
- MockMvc.

## Frontend Tests

Frontend tests are optional for the initial CV scope. If added, focus on smoke
tests for:

- Login.
- Event list.
- Order placement.

## Validation Ladder

| Layer | Expected proof |
| --- | --- |
| Unit | Domain and application business rules |
| Integration | Spring Security, controllers, repositories, MySQL, Redis, VNPAY adapter |
| E2E | Browser smoke flow from login through order/payment |
| Platform | Local MySQL/Redis startup, backend run, frontend run |
| Release | Full builds, API docs, onboarding report, screenshots or GIFs |

