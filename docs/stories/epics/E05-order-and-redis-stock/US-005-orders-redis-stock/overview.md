# US-005 Orders and Redis Stock

## Status

implemented

## Intake Classification

- Type: spec slice
- Lane: high-risk
- Reason: introduces authenticated order APIs, USER/ADMIN authorization rules, Redis stock reservation, MySQL stock consistency, order records, and public API behavior with missing proof.

## Goal

Implement the backend E05 slice so authenticated users can place, inspect, list, and cancel orders through `/api/orders` while Redis Lua gates ticket stock and MySQL remains the durable stock and order record.

## Scope

- `POST /api/orders`
- `GET /api/orders/my`
- `GET /api/orders/{orderId}`
- `PUT /api/orders/{orderId}/cancel`
- Redis stock key `TICKET_TYPE:{ticketTypeId}:STOCK`
- Redis Lua atomic stock decrement contract
- Cache-miss warmup from MySQL stock
- MySQL conditional stock update
- `orders` and `order_items` creation for pending orders
- USER ownership checks and ADMIN cancellation permission
- Backend application and controller tests

## Out Of Scope

- Payment state transitions, owned by E06.
- React order, checkout, and payment pages, owned by E07.
- VNPAY integration, owned by E06.
- Long-running stock reconciliation worker. This story documents the operational gap if Redis rollback fails.
