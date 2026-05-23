# Design

## Domain Model

Entities:

- `User`: account identity with email, password hash for local login, and roles.
- `Role`: `USER` or `ADMIN`.
- `Event`: public event with active state.
- `TicketType`: event ticket tier with price and stock.
- `Order`: booking transaction owned by a user.
- `OrderItem`: ticket type and quantity inside an order.
- `Payment`: payment attempt for an order.

Java package placement:

- Accepted product entities live directly in
  `com.xxxx.ddd.domain.model.entity`.
- The former `com.xxxx.ddd.domain.model.entity.ticketing` split is retired so
  this project can continue as a fresh ticket-booking build.
- Existing legacy entities are transitional compile support only until their
  services are migrated or removed by later stories.

Enums:

- Order status: `PENDING`, `PAID`, `CANCELLED`, `PAYMENT_FAILED`, `EXPIRED`.
- Payment status: `INIT`, `PENDING`, `SUCCESS`, `FAILED`.

## Application Flow

This story exposes persistence-ready domain and repository contracts for later
use cases. It should not orchestrate full user workflows yet.

## Interface Contract

API DTOs introduced later must use:

- `eventId`
- `ticketTypeId`
- `orderId`
- `paymentId`

No order API may accept vague `ticketId` for purchase behavior.

## Data Model

Accepted tables:

- `users`
- `roles`
- `user_roles`
- `events`
- `ticket_types`
- `orders`
- `order_items`
- `payments`
Refresh tokens are Redis-only for the active product scope, so this story does
not include a `RefreshToken` entity or `refresh_tokens` table.

Preferred additional table:

- `stock_adjustment_log`

US-002 defers `stock_adjustment_log` to the stock/reconciliation story because
this story does not implement Redis rollback behavior. E05 must either add the
table or document the operational reconciliation path when Redis rollback can
fail after a database change.

Important constraints:

- User email is unique.
- Role name is unique.
- Ticket type belongs to one event.
- Ticket type stock is non-negative.
- Ticket type price is positive.
- Order belongs to one user.
- Order item belongs to one order and one ticket type.
- Payment belongs to one order.

## UI / Platform Impact

No frontend UI is changed in this story. Seed data should make later frontend
smoke testing possible.

## Observability

No audit log table is required by this story unless auth or payment behavior is
pulled into scope. Schema and repository failures should be visible in test
logs.

## Alternatives Considered

1. Keep legacy ticket/ticket item naming. Rejected because the spec explicitly
   requires clear `eventId` and `ticketTypeId` naming.
2. Add monthly sharded order tables. Rejected because the spec removes this from
   the CV scope.
3. Store refresh tokens only in Redis. Accepted because it keeps the CV scope
   simpler and matches the selected runtime responsibility for Redis.
