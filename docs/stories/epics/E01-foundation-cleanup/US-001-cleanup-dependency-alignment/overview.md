# Overview

## Current Behavior

The repository already contains a Java/Spring multi-module backend and a React
Vite frontend. The current runtime and code include legacy ticket booking
concepts plus non-goal infrastructure and demos such as Kafka, Prometheus,
Grafana, ELK, Resilience4j demo config, employee demo behavior, API key demo
behavior, and benchmark artifacts.

## Target Behavior

The project keeps the DDD module shape, React frontend, MySQL, Redis, JPA, and
Redis stock concepts, but removes non-goal dependencies, required services,
demo controllers, and runtime config from the required local application. The
backend dependency versions should be aligned, and Spring Security dependencies
should be ready for the auth story.

## Affected Users

- Developer running the project locally.
- Recruiter or interviewer evaluating the CV project.
- Future agent implementing auth, data model, order, payment, and frontend
  stories.

## Affected Product Docs

- `docs/product/overview.md`
- `docs/product/local-development.md`
- `docs/product/validation.md`
- `docs/ARCHITECTURE.md`
- `docs/decisions/0004-accept-event-ticket-booking-redesign.md`

## Non-Goals

- Do not implement the new domain schema.
- Do not implement auth, orders, payments, or frontend rebuild.
- Do not delete optional documentation unless the implementation story confirms
  it is safe.
- Do not weaken the Redis Lua stock requirement.
