# Domain Model

## Core Concepts

| Concept | Purpose |
| --- | --- |
| `User` | Account that can log in and book tickets |
| `Role` | Authority assigned to a user; allowed values are `USER` and `ADMIN` |
| `Event` | Public event that users can view and book when active |
| `TicketType` | Ticket tier for an event, with price and available stock |
| `Order` | User booking transaction |
| `OrderItem` | Purchased ticket type and quantity inside an order |
| `Payment` | Payment attempt for an order |

## Java Entity Package

The accepted product entities live directly under
`com.xxxx.ddd.domain.model.entity`. The former `entity.ticketing` split was
removed because the project is being treated as a fresh ticket-booking build.

Transitional legacy entities may still exist only to keep earlier services
compiling until their flows are rewritten. They are not part of the accepted
US-002 product model.

## Naming Contract

The product must avoid ambiguity between legacy `ticketId` and
`ticket_item.id` naming.

| Name | Meaning |
| --- | --- |
| `eventId` | ID of the event |
| `ticketTypeId` | ID of the ticket tier being purchased |
| `orderId` | ID of the user order |
| `paymentId` | ID of the payment record |

All order APIs must accept `ticketTypeId`, not vague `ticketId`.

## Suggested Tables

- `users`
- `roles`
- `user_roles`
- `events`
- `ticket_types`
- `orders`
- `order_items`
- `payments`

Refresh tokens are Redis-only for the active product scope. Do not add a
`refresh_tokens` table or `RefreshToken` entity unless a later auth decision
explicitly adds DB-backed session metadata.

If Redis rollback can fail after a successful stock decrement, the preferred
schema also includes `stock_adjustment_log`. If this table is deferred, the
stock story must document the follow-up reconciliation gap.

US-002 deferred `stock_adjustment_log`; E05 owns the final reconciliation table
or operational follow-up decision when Redis rollback behavior is implemented.

## Order Statuses

- `PENDING`
- `PAID`
- `CANCELLED`
- `PAYMENT_FAILED`
- `EXPIRED`

## Payment Statuses

- `INIT`
- `PENDING`
- `SUCCESS`
- `FAILED`

## Business Rules

- Only active events are visible to public users.
- A ticket type belongs to exactly one event.
- Ticket type stock must not be negative.
- Ticket type price must be positive.
- An event cannot be deleted if it has paid orders; it can be marked inactive.
- Only `PENDING` orders can be cancelled.
- A `USER` can cancel only their own pending order.
- An `ADMIN` can cancel any pending order when business rules allow it.
- Cancelling an order restores MySQL stock and Redis stock.
