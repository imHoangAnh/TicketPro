# Overview

## Current Behavior

The existing project uses legacy ticket names and concepts that do not cleanly
separate event, ticket tier, order, and order item. The spec explicitly calls
out ambiguity between `ticketId` and `ticket_item.id`.

## Target Behavior

The backend has a clean event ticket booking schema and domain naming contract:

- `eventId` means event ID.
- `ticketTypeId` means ticket tier ID.
- `orderId` means user order ID.
- `paymentId` means payment record ID.

The schema supports users, roles, events, ticket types, orders, order items,
payments. Refresh token state is Redis-only and is not part of the MySQL domain
model.

## Affected Users

- `USER` placing orders and viewing own order history.
- `ADMIN` managing events, ticket types, stock, and orders.
- Developer implementing auth, order, payment, and frontend stories.

## Affected Product Docs

- `docs/product/domain-model.md`
- `docs/product/api.md`
- `docs/product/orders-and-stock.md`
- `docs/product/payments.md`
- `docs/product/auth-and-authorization.md`
- `docs/product/validation.md`

## Non-Goals

- Do not implement full auth behavior.
- Do not implement Redis Lua order placement.
- Do not implement payment flows.
- Do not rebuild the frontend.
- Do not add monthly sharded order tables.
