# Test Matrix

This file maps product behavior to proof.

Product behavior was seeded from `SPEC.MD` on 2026-05-22. Do not mark a row
implemented until tests or validation evidence exist.

## Status Values

| Status | Meaning |
| --- | --- |
| planned | Accepted as intended behavior, not implemented |
| in_progress | Actively being built |
| implemented | Implemented and proof exists |
| changed | Contract changed after earlier implementation |
| retired | No longer part of the product contract |

## Matrix

| Story | Contract | Unit | Integration | E2E | Platform | Status | Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `docs/stories/epics/E01-foundation-cleanup/US-001-cleanup-dependency-alignment/` | Required local runtime keeps MySQL and Redis, removes non-goal dependencies/services/demo behavior, and keeps DDD modules usable | pass: `mvn test` | pass: `mvn package` | no | pass: `docker-compose config`, frontend build pass | implemented | Backend passed with temporary `JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8`; frontend build passed; Docker Compose config passed with Docker config warning; code review found no critical issues and important issues were fixed |
| `docs/stories/epics/E02-domain-data-model/US-002-event-ticket-data-model/` | Schema and domain names use users, roles, events, ticket_types, orders, order_items, payments, root-package entities, Redis-only refresh tokens, and `ticketTypeId` | pass: domain validation tests | pass: repository and init SQL tests | no | pass: `docker-compose config` | implemented | `mvn test` and `mvn package` passed with Java 21; accepted product entities moved to `com.xxxx.ddd.domain.model.entity`; `RefreshToken`/`refresh_tokens` removed after Redis-only decision; H2 MySQL-mode test executed local init SQL and verified accepted tables/seed data; order request surface uses `ticketTypeId`; Docker Compose config passed with Docker config warning |
| `docs/stories/epics/E03-auth-and-authorization/US-003-email-password-jwt-refresh/` | Email/password auth issues JWT access tokens, stores opaque hashed refresh tokens in Redis only, rotates refresh atomically, invalidates logout, exposes `/api/auth/me`, and enforces USER/ADMIN route rules | pass: auth service/JWT/atomic refresh tests | pass: MockMvc security and auth cookie/status tests | no | pass: `mvn package`, `docker-compose config` | implemented | `mvn test` and `mvn package` passed with Java 21; `docker-compose config` passed with Docker config warning; code review found non-atomic refresh and cookie-guard issues, fixes were applied; refresh/logout require `X-Requested-With: XMLHttpRequest`; legacy `/order/**` and `/payment/**` restricted to `ADMIN` until E05/E06 replace them |
| `docs/stories/epics/E04-event-and-ticket-management/US-004-public-admin-events-ticket-types/` | Public users can browse active events and active ticket types; admins can create/update/activate/inactivate/delete events and create/update/delete ticket types through `/api/events` and `/api/admin/**`; delete is inactive soft delete; mutations invalidate accepted Redis event/ticket type cache keys | pass: event service filtering, validation, soft-delete, and cache invalidation tests | pass: MockMvc public/admin route and status tests | no | pass: `mvn package`, `docker-compose config` | implemented | `mvn -pl xxxx-controller -am test`, `mvn test`, and `mvn package` passed with Java 21; `docker-compose -f environment\docker-compose-dev.yml config` passed with Docker config warning |
| `docs/stories/epics/E05-order-and-redis-stock/US-005-orders-redis-stock/` | Authenticated users can place and cancel orders through `/api/orders`; Redis Lua gates stock with MySQL conditional stock update and rollback behavior; USER ownership and ADMIN access are enforced | pass: order service and Redis stock tests | pass: MockMvc order route and authorization tests | no | pass: `mvn package`, `docker-compose config` | implemented | `mvn -pl xxxx-controller -am test`, `mvn test`, and `mvn package` passed with Java 21; `docker-compose -f environment\docker-compose-dev.yml config` passed with Docker config warning |

| `docs/stories/epics/E06-payments/US-006-mock-and-vnpay-payments/` | Authenticated users can pay for PENDING orders through mock payment and VNPAY sandbox; VNPAY callback verifies signature and transitions payment/order status; USER ownership and ADMIN bypass enforced; VNPAY config externalized | pass: mock/VNPAY/callback/ownership/status tests (13 cases) | pass: MockMvc route auth, payment success/error mapping, callback redirect tests (11 cases) | no | pass: `mvn package`, `docker-compose config` | implemented | `mvn test` passed (30 total); `mvn package` BUILD SUCCESS; `docker-compose config` passed |
| `docs/stories/epics/E07-frontend-rebuild/US-007-frontend-rebuild-ui-ux/` | React pages are rebuilt around clean `/api/*` endpoints; includes login, register, private routing, order creation, payment selector (Mock Success, VNPAY Sandbox redirect/callback), and a premium multi-tab Admin dashboard | changed: API layer/component imports currently drift (`ticketService` and `managerService` missing from `api.js`) | changed: Axios interceptor exists, but frontend validation is blocked by lint/build failures | no | fail: `npm run lint`; fail: `npm run build` | changed | 2026-05-23 review found stale evidence: lint has 8 errors and build fails because components import missing `ticketService` and `managerService`; E07 needs a stabilization pass before it can be marked implemented |
| `docs/stories/epics/E01-foundation-cleanup/US-008-ddd-boundary-refactor/` | Backend modules enforce accepted DDD dependency direction through application ports, infrastructure adapters, domain boundary cleanup, and mechanical architecture checks without changing accepted API behavior | pass: full reactor `mvn test`, application port tests, `ArchitectureBoundaryTest` | pass: repository adapter tests, controller tests, infrastructure tests covered by full reactor | no: API behavior unchanged | pass: `mvn package`; pass: `docker-compose -f environment\docker-compose-dev.yml config` with existing Docker config warning | implemented | Removed `xxxx-application` dependency on `xxxx-infrastructure`; added cache, stock script, distributed lock, and payment gateway ports; infrastructure implements those ports; `mvn test`, escalated `mvn package`, and Docker Compose config passed with Java 21; architecture test enforces forbidden dependency/import rules |

## Evidence Rules

- Unit proof covers pure domain and application rules.
- Integration proof covers backend enforcement, data integrity, provider
  behavior, jobs, or service contracts.
- E2E proof covers user-visible browser flows.
- Platform proof covers only shell, deployment, mobile, desktop, or runtime
  behavior that cannot be proven in lower layers.
- A story can be implemented without every proof column if the story packet
  explains why.
