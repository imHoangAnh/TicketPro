# US-004 Public and Admin Event Management

## Status

implemented

## Intake Classification

- Type: spec slice
- Lane: high-risk
- Reason: introduces public and admin API contracts, changes authorization surface, mutates event/ticket type data, and requires cache invalidation proof.

## Goal

Implement E04 so customers can browse active events and ticket types while admins can create, update, activate, inactivate, and delete events and ticket types through the accepted `/api/events` and `/api/admin/**` contracts.

## Scope

- Public `GET /api/events`
- Public `GET /api/events/{eventId}`
- Admin event create/update/delete/activate/inactivate
- Admin ticket type create/update/delete
- Event/ticket type validation from the domain model
- Redis cache key invalidation for accepted event and ticket type keys
- Controller and application tests for behavior and route protection

## Out Of Scope

- Order placement and Redis stock reservation, owned by E05.
- Payment flows, owned by E06.
- Frontend rebuild, owned by E07.
- Google OAuth2, still outside this E04 slice.
