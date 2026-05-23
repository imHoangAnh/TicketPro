# Story Backlog

This backlog was seeded from `SPEC.MD` on 2026-05-22. Do not create every
possible story packet up front. Create story packets when the work is selected
or when a product decision needs a durable place to land.

## Candidate Epics

| Epic | Description | Status |
| --- | --- | --- |
| E01 Foundation cleanup | Remove non-goal infrastructure and demo behavior while keeping DDD modules, JPA, Redis, and React | sliced |
| E02 Domain data model | Replace ambiguous ticket naming with event/ticket type/order/payment/auth schema | sliced |
| E03 Auth and authorization | Implement email/password, refresh token rotation, Google OAuth2, and USER/ADMIN access control | sliced |
| E04 Event and ticket management | Implement public event browsing and admin event/ticket type management with cache invalidation | sliced |
| E05 Order and Redis stock | Implement Redis Lua stock reservation, DB consistency checks, cancellation, and reconciliation notes | sliced |
| E06 Payments | Implement mock payment and VNPAY sandbox URL/callback flow | sliced |
| E07 Frontend rebuild | Rebuild React pages around the accepted API shape and protected route behavior | sliced |
| E08 Documentation and polish | Produce API docs, onboarding report, run guide, screenshots, and CV summary | unsliced |
| E09 DDD architecture hardening | Refactor backend module boundaries and ports/adapters to enforce the accepted DDD dependency direction | sliced |

## Active Story Packets

| Story | Epic | Status |
| --- | --- | --- |
| `docs/stories/epics/E01-foundation-cleanup/US-001-cleanup-dependency-alignment/` | E01 | implemented |
| `docs/stories/epics/E02-domain-data-model/US-002-event-ticket-data-model/` | E02 | implemented |
| `docs/stories/epics/E03-auth-and-authorization/US-003-email-password-jwt-refresh/` | E03 | implemented |
| `docs/stories/epics/E04-event-and-ticket-management/US-004-public-admin-events-ticket-types/` | E04 | implemented |
| `docs/stories/epics/E05-order-and-redis-stock/US-005-orders-redis-stock/` | E05 | implemented |
| `docs/stories/epics/E06-payments/US-006-mock-and-vnpay-payments/` | E06 | implemented |
| `docs/stories/epics/E07-frontend-rebuild/US-007-frontend-rebuild-ui-ux/` | E07 | in_progress |
| `docs/stories/epics/E01-foundation-cleanup/US-008-ddd-boundary-refactor/` | E09 | implemented |
