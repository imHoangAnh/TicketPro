# Validation

## Proof Strategy

Proof should show that the accepted schema, domain names, and persistence
constraints support later auth, order, stock, and payment stories without
ambiguous IDs.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Domain constructors/factories reject negative stock, non-positive price, invalid statuses, and invalid role names |
| Integration | Schema creates all accepted tables; unique email and role constraints work; ticket type belongs to event; conditional stock update works |
| E2E | Not required for this data model story |
| Platform | Local MySQL init or migration runs from a clean database |
| Performance | Not required |
| Logs/Audit | Migration/schema errors are visible in test output |

## Fixtures

- One admin user.
- One normal user.
- One active event with at least two ticket types.
- One inactive event.
- One pending order fixture if repository tests require it.

## Commands

```text
mvn test
mvn package
docker-compose -f environment/docker-compose-dev.yml config
git diff --check
```

## Acceptance Evidence

- Backend environment: commands were run with temporary
  `JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8` and `PATH` prepended
  in this shell. Maven needed approved network access to resolve the Spring
  Boot BOM and new test dependencies.
- `mvn test`: passed for the full reactor. New US-002 proof includes 4 domain
  validation tests and 3 infrastructure/schema tests.
- Entity package refactor: passed. Accepted product entities now live directly
  under `com.xxxx.ddd.domain.model.entity`; no Java source imports
  `com.xxxx.ddd.domain.model.entity.ticketing`.
- `mvn package`: passed for the full reactor and repackaged `xxxx-start`.
- `TicketingDomainValidationTest`: passed. It verifies positive ticket type
  price, non-negative stock, `ticketTypeId` order item naming, valid event
  time range, and role names.
- `TicketingRepositoryIntegrationTest`: passed. It verifies repository
  persistence for users, roles, user roles, events, ticket types, orders, order
  items, payments, unique user email, active event lookup,
  conditional stock decrement, insufficient stock protection, and paid-order
  detection for event delete protection.
- `TicketInitSqlTest`: passed. It executes
  `environment/mysql/init/ticket_init.sql` in H2 MySQL mode, verifies accepted
  tables and seed data, and verifies invalid ticket type foreign keys fail.
- `docker-compose -f environment/docker-compose-dev.yml config`: passed and
  showed MySQL and Redis as required local services. Docker still warned that it
  could not read `C:\Users\trhoa\.docker\config.json`.
- Contract scan: passed for the updated order request surface. `CreateBookingRequest`,
  `TicketOrderController`, and `StockOrderCacheService` no longer expose
  `ticketId`, legacy SQL table names, or legacy Redis `TICKET:` stock keys.
- Stock persistence alignment: `TicketOrderJPAMapper` now reads and updates
  `TicketType`/`ticket_types` for stock checks instead of legacy
  `TicketDetail`/`ticket_item`.
- `git diff --check`: passed. Git reported LF-to-CRLF warnings on Windows, not
  whitespace errors.

## Residual Transitional Scope

Legacy entities such as `Ticket`, `TicketDetail`, `TickerOrder`,
`PaymentTransaction`, and `Booking` remain only because earlier services and
controllers still compile against them. The accepted clean product model is the
root-package US-002 model above; later API/order/payment stories should migrate
or remove legacy flows instead of adding new behavior to those classes.

Refresh token state is Redis-only. `RefreshToken` and `refresh_tokens` were
removed from the active US-002 model after the storage decision was made.

US-002 is implemented. No E2E proof was required for this data model story.
