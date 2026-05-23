# Architecture

The project is an event ticket booking system built on a DDD-flavoured
multi-module Java/Spring Boot backend and a React frontend. The accepted
product direction from `SPEC.MD` keeps the existing multi-module backend while
redesigning the application into the event ticket booking domain.

## Selected Stack

- Backend: Java 21, Spring Boot, Maven multi-module project.
- Frontend: React + Vite in `xxxx.fe.com`.
- Persistence: MySQL.
- Redis: refresh tokens, cache, and ticket stock.
- Security: Spring Security, JWT access tokens, opaque refresh tokens, Google
  OAuth2, USER and ADMIN roles.
- Payments: mock payment and VNPAY sandbox.
- Required local infrastructure: MySQL and Redis only.

## Module Boundaries

Declared modules in the root:

```text
xxxx-start
  -> xxxx-controller
  -> xxxx-infrastructure

xxxx-controller
  -> xxxx-application

xxxx-application
  -> xxxx-domain

xxxx-infrastructure
  -> xxxx-domain
  -> xxxx-application contracts when needed
```

US-008 removed the previous direct compile-scope dependency from
`xxxx-application` to `xxxx-infrastructure`. Application now owns provider
ports for cache, distributed lock, and payment gateway concerns; infrastructure
implements those contracts as adapters.

## Target Module Boundaries

The target dependency direction remains:

```text
xxxx-start
  -> xxxx-controller
  -> xxxx-infrastructure

xxxx-controller
  -> xxxx-application

xxxx-application
  -> xxxx-domain

xxxx-infrastructure
  -> xxxx-domain
  -> xxxx-application contracts when needed
```

In this target, `xxxx-start` is the runtime composition root. It may depend on
the outer modules needed to boot the Spring application, but application code
must not depend on infrastructure implementations.

Runtime flow:

```text
REST controller (xxxx-controller)
  -> application use case (xxxx-application)
      -> domain service / repository interface (xxxx-domain)
          -> infrastructure adapter
              -> MySQL, Redis, Google OAuth2, or VNPAY
```

`xxxx.fe.com` consumes the REST API and must not depend on backend internals.

Responsibilities:

- `xxxx-start`: bootstrap, application config, profiles, and Spring Boot main
  class.
- `xxxx-controller`: REST controllers, DTOs, request validation, exception
  handling, and web security entry points.
- `xxxx-application`: use case orchestration, transaction boundaries, auth,
  token, order, payment, and cache orchestration.
- `xxxx-domain`: entities, value objects, enums, domain services, repository
  interfaces, and business rules.
- `xxxx-infrastructure`: JPA adapters, Redis adapter, Redis Lua stock adapter,
  JWT provider, OAuth2 adapter, and VNPAY gateway adapter.

## Product Domains

- Auth and authorization.
- Events and ticket types.
- Orders and stock reservation.
- Payments.
- Frontend route surfaces.
- Local runtime and documentation.

## Dependency Rule

Inner layers must not depend on outer layers.

| Layer | May depend on | Must not depend on |
| --- | --- | --- |
| domain | domain types, domain services, repository contracts | UI, provider SDKs, process/env, concrete infrastructure clients |
| application | domain | UI, provider SDKs, database concrete clients, infrastructure adapters |
| infrastructure | domain, application contracts when needed | controller or UI |
| interface | application | infrastructure adapters, UI state, platform shell assumptions |
| app surfaces | API contracts and app-facing clients | domain internals directly |

## Parse-First Boundary Rule

Unknown data must be parsed at boundaries before it enters inner code.

Boundaries include:

- HTTP request bodies, params, and query strings.
- Session payloads and identity claims.
- Environment variables.
- Database rows returned from external clients.
- Platform shell payloads.
- Deep links, tokens, and signed URLs.
- Provider webhooks, events, and async payloads.

Target flow:

```text
unknown input
  -> parser
  -> typed DTO or command
  -> application use case
  -> domain object/value object
```

Inner layers should work with meaningful product types such as `UserId`,
`EventId`, `TicketTypeId`, `OrderId`, `PaymentId`, `Role`, or
domain-specific IDs, rather than repeatedly validating raw strings.

## Command/Query Boundary

If the product has both reads and writes, keep command/query separation clear at
the code level even when the storage layer is simple:

- Commands mutate state and own audit side effects.
- Queries read state and format for consumers.
- Shared domain rules live in domain/application, not controllers.

## Observability Contract

The server should emit one canonical JSON log line per request with:

- timestamp
- level
- request_id
- user_id when known
- action
- duration_ms
- status_code
- message

Audit logs are product records. Application logs are operational records. Do not
use one as a substitute for the other.

## Removed Required Runtime

The target product removes Kafka, RabbitMQ, Prometheus, Grafana, ELK, demo
Resilience4j endpoints, employee sign-in demo, API key secure demo, monthly
sharded order tables, and benchmark artifacts from required local runtime and
core implementation scope.
