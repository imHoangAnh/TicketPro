# Design

## API Shape

Authenticated user endpoints:

- `POST /api/orders`
- `GET /api/orders/my`
- `GET /api/orders/{orderId}`
- `PUT /api/orders/{orderId}/cancel`

The accepted request shape remains:

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

Order APIs use authenticated principal data from the JWT filter. Callers cannot pass `userId` in the path or body to act as another user.

## Application Rules

- Quantity must be positive.
- Ticket type must exist and be active.
- The parent event must exist and be active.
- Redis Lua is the first stock gate.
- Redis cache miss warms `TICKET_TYPE:{ticketTypeId}:STOCK` from MySQL and retries once.
- Insufficient Redis stock returns a stable out-of-stock response.
- Successful Redis decrement is followed by a transactional MySQL conditional stock update and order/order item creation.
- If MySQL update or order persistence fails after Redis reservation, Redis is incremented back.
- Only `PENDING` orders can be cancelled.
- `USER` can cancel only their own order.
- `ADMIN` can cancel any pending order.
- Cancelling restores MySQL stock and Redis stock.

## Interface Contract

`POST /api/orders` returns an order placement response with success status, stable code/message on failure, and the created order id on success.

`GET /api/orders/my` returns the authenticated user's orders.

`GET /api/orders/{orderId}` returns the order only when the caller owns it or has `ADMIN`.

`PUT /api/orders/{orderId}/cancel` returns success when cancellation completes and stable failure responses for missing, forbidden, already-paid, already-cancelled, or stock-restore failures.

## Data Model

This story uses accepted US-002 tables:

- `orders`
- `order_items`
- `ticket_types`
- `events`

No monthly sharded order table is part of E05. Existing legacy order classes can remain only where needed for compile compatibility, but new E05 behavior uses the accepted entities directly.

## Observability

Application logs should include order placement and cancellation outcomes with `userId`, `orderId`, `ticketTypeId`, and failure code where available. Redis rollback failure is logged as an operational inconsistency. A durable reconciliation table is deferred unless implementation shows it is necessary for safe completion.

## Alternatives Considered

1. MySQL-only stock decrement. Rejected because the product contract explicitly requires Redis Lua stock reservation.
2. Redis-only stock decrement. Rejected because MySQL remains the durable order and stock record.
3. Reuse legacy `/order/**` monthly table flow. Rejected because it conflicts with the accepted `/api/orders` contract and E01 removal of sharded order scope.
