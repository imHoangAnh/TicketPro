# Design

## API Shape

Public endpoints stay unauthenticated:

- `GET /api/events`
- `GET /api/events/{eventId}`

Admin endpoints require `ADMIN` through the existing E03 `/api/admin/**` rule:

- `POST /api/admin/events`
- `PUT /api/admin/events/{eventId}`
- `DELETE /api/admin/events/{eventId}`
- `PUT /api/admin/events/{eventId}/active`
- `PUT /api/admin/events/{eventId}/inactive`
- `POST /api/admin/events/{eventId}/ticket-types`
- `PUT /api/admin/ticket-types/{ticketTypeId}`
- `DELETE /api/admin/ticket-types/{ticketTypeId}`

## Application Rules

- Public event listing returns only active events.
- Public event detail returns only active events and active ticket types.
- Admin detail responses include all ticket types for operational visibility.
- Event validation remains in `Event.validate()`.
- Ticket type validation remains in `TicketType.validate()`.
- Delete endpoints inactivate records instead of hard-deleting them. This keeps order history and foreign keys stable; paid events are therefore naturally preserved and removed from public browsing by becoming inactive.

## Cache Rules

Accepted cache keys are invalidated on mutation:

- `EVENT:LIST:ACTIVE`
- `EVENT:{eventId}`
- `TICKET_TYPE:{ticketTypeId}`

The story only requires mutation invalidation. Read-through event caching can be added later if it becomes necessary for E05/E07 performance work.
