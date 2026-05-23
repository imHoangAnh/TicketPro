# Spec Intake

Date: 2026-05-22

## Source

- User prompt: "using harnesVo to impliment [SPEC.MD](SPEC.MD)"
- Attached file: `SPEC.MD`
- Input type: New spec
- Lane: High-risk

## Project Summary

The accepted product direction is to redesign the existing repository into a
clean event ticket booking CV project for a fresher Java/Spring position. The
project should be easy to run locally, easy to explain in an interview, and
complete enough to demonstrate backend fundamentals, frontend integration,
security, transactions, Redis caching, Redis Lua stock reservation, and payment
integration.

This intake is harness-only. It converts the spec into living product
contracts, story candidates, validation expectations, and decisions. It does
not change application code.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Data model.
- Audit/security.
- External systems.
- Public contracts.
- Cross-platform.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Auth.
- Authorization.
- Data model.
- External provider behavior.

Result: High-risk. The full spec crosses backend, frontend, security, database,
Redis, VNPAY, and local runtime concerns.

## Candidate Product Docs

| File | Purpose | Source sections |
| --- | --- | --- |
| `docs/product/overview.md` | Product goal, scope, users, and success criteria | 1, 4, 14, 15 |
| `docs/product/domain-model.md` | Domain concepts, IDs, statuses, and business rules | 3, 6, 7, 8, 9 |
| `docs/product/auth-and-authorization.md` | Auth model, token lifecycle, OAuth2, role access | 4, 5, 10, 11 |
| `docs/product/api.md` | Public, auth, user, and admin API groups | 5, 6, 7, 8, 9, 10 |
| `docs/product/events-and-ticket-types.md` | Public event browsing and admin event/ticket management | 6 |
| `docs/product/orders-and-stock.md` | Redis Lua stock reservation, order placement, cancellation | 7, 8 |
| `docs/product/payments.md` | Mock payment and VNPAY sandbox behavior | 9 |
| `docs/product/frontend.md` | React route surfaces and auth behavior | 11 |
| `docs/product/local-development.md` | Local runtime stack, docs, and removed required services | 12 |
| `docs/product/validation.md` | Expected proof ladder and priority test cases | 13 |

## Candidate Epics

| Epic | Description | Status |
| --- | --- | --- |
| E01 Foundation cleanup | Remove non-goal infrastructure and demo behavior while keeping DDD modules, JPA, Redis, and React | sliced |
| E02 Domain data model | Replace ambiguous ticket naming with event/ticket type/order/payment/auth schema | sliced |
| E03 Auth and authorization | Implement email/password, refresh token rotation, Google OAuth2, and USER/ADMIN access control | unsliced |
| E04 Event and ticket management | Implement public event browsing and admin event/ticket type management with cache invalidation | unsliced |
| E05 Order and Redis stock | Implement Redis Lua stock reservation, DB consistency checks, cancellation, and reconciliation notes | unsliced |
| E06 Payments | Implement mock payment and VNPAY sandbox URL/callback flow | unsliced |
| E07 Frontend rebuild | Rebuild React pages around the accepted API shape and protected route behavior | unsliced |
| E08 Documentation and polish | Produce API docs, onboarding report, run guide, screenshots, and CV summary | unsliced |

## Architecture Questions

- Runtime stack: Java 21, Spring Boot, Maven multi-module backend, React + Vite frontend.
- Product surfaces: REST API and browser frontend.
- Storage: MySQL for persistence; Redis for refresh tokens, cache, and stock.
- External providers: Google OAuth2 and VNPAY sandbox.
- Deployment target: local development/demo first, with MySQL and Redis as the required local services.
- Security model: JWT access token, opaque refresh token, Spring Security, USER and ADMIN roles.

## Validation Shape

| Layer | Expected proof |
| --- | --- |
| Unit | Domain and application rules for auth, orders, stock, payment status, and authorization predicates |
| Integration | Spring MVC/Security, MySQL schema/repositories, Redis stock scripts, Redis refresh token rotation, VNPAY signature adapter |
| E2E | Browser smoke flows for login, event detail, order placement, payment, order history, and admin management |
| Platform | Local MySQL/Redis Docker Compose startup and backend/frontend run guides |
| Release | Full Maven and frontend builds, API docs, screenshots or GIFs, and final onboarding report |

## Open Decisions

- Refresh token metadata is Redis-only for the initial CV scope; MySQL
  `refresh_tokens` is not part of the active schema.
- Whether a `stock_adjustment_log` table is implemented in the first stock story or documented as a follow-up.
- Whether frontend access tokens use memory or local storage for the first demo version.
- Which migration tool, if any, owns schema creation.

## First Story Candidates

- `docs/stories/epics/E01-foundation-cleanup/US-001-cleanup-dependency-alignment/`
- `docs/stories/epics/E02-domain-data-model/US-002-event-ticket-data-model/`

## Harness Delta

The spec is large enough that the harness should continue using candidate epics
plus story packets rather than creating a second monolithic spec. No harness
process change is required in this pass.
