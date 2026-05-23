# Events And Ticket Types

## Public Event Browsing

Users can view active events and event detail:

```text
GET /api/events
GET /api/events/{eventId}
```

Only active events appear in public results.

## Admin Event Management

Admins can create, update, activate, inactivate, and delete events:

```text
POST   /api/admin/events
PUT    /api/admin/events/{eventId}
DELETE /api/admin/events/{eventId}
PUT    /api/admin/events/{eventId}/active
PUT    /api/admin/events/{eventId}/inactive
```

Event delete is implemented as an inactive soft delete. This keeps order
history and foreign keys stable, including the required paid-order case where
the event must not be physically removed.

## Admin Ticket Type Management

Admins can create, update, and delete ticket types:

```text
POST   /api/admin/events/{eventId}/ticket-types
PUT    /api/admin/ticket-types/{ticketTypeId}
DELETE /api/admin/ticket-types/{ticketTypeId}
```

Ticket types belong to one event. Price must be positive and stock must be
non-negative. Ticket type delete is implemented as an inactive soft delete so
existing order items can keep their historical references.

## Redis Cache

Accepted cache keys:

```text
EVENT:LIST:ACTIVE
EVENT:{eventId}
TICKET_TYPE:{ticketTypeId}
```

## Cache Invalidation

- Event create, update, delete, activate, or inactivate invalidates active event
  list and event detail caches.
- Ticket type create, update, or delete invalidates event detail and ticket type
  caches.
