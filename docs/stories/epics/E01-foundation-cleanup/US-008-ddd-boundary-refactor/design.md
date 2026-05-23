# Design

## Domain Model

Keep the accepted product concepts from `docs/product/domain-model.md`:

- `User`
- `Role`
- `Event`
- `TicketType`
- `Order`
- `OrderItem`
- `Payment`

The refactor does not change the domain vocabulary. It changes where technical
responsibilities live.

Target domain rules for this story:

- Repository interfaces stay in `xxxx-domain` and describe product intent, not
  storage details.
- Domain services and entities remain the place for business rules and
  invariants.
- Domain code must not depend on infrastructure adapters such as Redis,
  VNPAY, JWT, OAuth2, or JPA repository implementations.
- Existing JPA annotations inside domain entities are accepted as transitional
  Spring-DDD coupling for this project. Removing them is not required to close
  this story unless the implementation naturally reaches that boundary.

The first implementation should prefer this staged cleanup:

1. Stop `xxxx-application` from depending on `xxxx-infrastructure`.
2. Move provider/cache contracts into `xxxx-application` contracts or
   `xxxx-domain` repository/service interfaces depending on ownership.
3. Make infrastructure implement those contracts.
4. Add a mechanical architecture check that fails if application imports
   infrastructure again.

## Application Flow

Application services remain the use case boundary. They should orchestrate:

- request-level command/query handling from controllers,
- transaction boundaries,
- authorization decisions passed from authenticated principals,
- repository calls through domain repository interfaces,
- cache, token, gateway, and provider calls through application ports,
- DTO mapping for controller-facing responses.

Introduce contracts where concrete infrastructure is currently imported by
application code. Candidate contracts:

| Contract | Owner | Current concrete concern |
| --- | --- |
| `PaymentGateway` | `xxxx-application` contract | VNPAY URL creation and callback signature verification |
| `StockCachePort` | `xxxx-application` contract | Redis Lua stock decrement, warmup, and rollback |
| `RefreshTokenRepository` | `xxxx-domain` or existing domain auth contract | Redis refresh token session storage |
| `TokenProvider` / `TokenIssuer` | `xxxx-application` contract | JWT creation and parsing |
| `EventCachePort` | `xxxx-application` contract | event and ticket type cache invalidation |
| `DistributedLockPort` | `xxxx-application` contract if still needed | Redisson locks retained for legacy or future flows |

Infrastructure adapters implement those contracts and are wired by Spring at
runtime. Application tests should mock contracts instead of importing
infrastructure classes.

## Interface Contract

No accepted API path changes are introduced by this story.

Accepted routes remain:

- `/api/auth/**`
- `/api/events/**`
- `/api/admin/**`
- `/api/orders/**`
- `/api/payments/**`

Legacy routes are handled as follows:

- `/ticket/**`
- `/order/**`
- `/payment/**`

This story may isolate legacy routes behind clear compatibility boundaries or
remove them only if product docs and tests prove no accepted frontend/API path
uses them. If route removal is needed, it must be called out before
implementation or split into a separate cleanup story.

Error response shapes should remain stable enough for the frontend retry,
redirect, and display behavior described in `docs/product/api.md`.

## Data Model

No schema migration is required for the first DDD boundary pass.

Data model changes are allowed only when they directly support architecture
separation and have explicit proof. Examples:

- Add a unique constraint for payment idempotency if the payment story accepts
  one payment attempt policy per order.
- Add a stock reconciliation table only if the stock story is updated to own
  operational recovery.
- Split JPA persistence entities from domain objects only if the resulting
  migration has repository and SQL proof.

JPA mapping remains acceptable in the current DDD-flavoured Spring model for
this story. A stricter persistence-model/domain-model split can be proposed
later, but it is not required for the dependency-gap cleanup.

## UI / Platform Impact

The frontend should not need behavior changes from this story.

The story may expose existing frontend/API drift as a blocker, but frontend
build repair belongs to E07 unless the DDD refactor directly changes a backend
contract. Local runtime remains Spring Boot, MySQL, Redis, and React/Vite.

## Observability

Preserve existing application logs for auth, order, payment, and stock flows.
When introducing ports, keep log lines at the application boundary for product
decisions and at the infrastructure adapter boundary for provider/client
failures.

The refactor should not use application logs as audit records. If audit
behavior is introduced or required, it needs a separate product decision.

## Architecture Guardrails

Add a lightweight architecture check before or during implementation. The check
should fail when:

- `xxxx-application/pom.xml` depends on `xxxx-infrastructure`.
- `xxxx-application` imports `com.xxxx.ddd.infrastructure`.
- `xxxx-infrastructure` imports controller classes.
- controller classes import infrastructure classes directly.

The check may warn, but should not fail, for existing Spring/JPA annotations in
`xxxx-domain` unless a later story accepts a stricter pure-domain target.

The first version may be a Maven test or a small source-scan script, but it
must be runnable in the normal validation flow.

## Alternatives Considered

1. Big-bang rewrite into strict pure-domain hexagonal architecture. Rejected because the
   backend currently passes tests and contains multiple accepted stories; a
   full rewrite would make regressions harder to isolate.
2. Leave the current module dependencies alone and document the drift. Rejected
   because the project explicitly presents DDD as a success criterion.
3. Staged boundary inversion with architecture checks. Accepted because it
   protects current behavior while making every step reviewable.
