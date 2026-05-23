# API Contract

## Public

```text
GET /api/events
GET /api/events/{eventId}
```

## Auth

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
GET  /oauth2/authorization/google
GET  /login/oauth2/code/google
```

## User

```text
POST /api/orders
GET  /api/orders/my
GET  /api/orders/{orderId}
PUT  /api/orders/{orderId}/cancel
POST /api/payments/{orderId}/mock-success
POST /api/payments/{orderId}/vnpay
```

## Admin

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

## Order Request

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

## Contract Rules

- Order APIs must use `ticketTypeId`.
- Public event APIs return only active events.
- Admin APIs require `ADMIN`.
- User order and payment APIs require authentication.
- API errors should be stable enough for frontend retry, redirect, and error
  display behavior.

