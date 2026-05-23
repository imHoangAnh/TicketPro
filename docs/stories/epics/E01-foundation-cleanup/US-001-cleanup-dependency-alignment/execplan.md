# Exec Plan

## Goal

Prepare the existing project for the event ticket booking redesign by removing
non-goal runtime dependencies and demo behavior while preserving the module
shape and required MySQL/Redis local stack.

## Scope

In scope:

- Inventory backend dependencies and runtime config.
- Remove Kafka from required application code and config.
- Remove monitoring-only dependencies and required Docker services.
- Remove or retire demo controllers and services that are not part of the new
  product.
- Keep Redis and JPA.
- Align Spring Boot versions.
- Add Spring Security dependencies needed by the later auth story.
- Keep React + Vite frontend in place.

Out of scope:

- New schema/migrations.
- Auth implementation.
- New event/order/payment APIs.
- Frontend route rebuild.
- CI creation.

## Risk Classification

Risk flags:

- External systems.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- External provider behavior because Kafka and monitoring services are being
  removed from required runtime.

Lane: high-risk.

## Work Phases

1. Inventory dependencies, controllers, services, and Docker Compose entries.
2. Decide which artifacts are deleted, retired, or kept as optional docs.
3. Update Maven dependency tree and application config.
4. Update Docker Compose to require only MySQL and Redis.
5. Remove demo endpoints or isolate them from the target product.
6. Run backend and frontend build checks when scripts exist.
7. Update docs, test matrix evidence, and harness notes.

## Stop Conditions

Pause for human confirmation if:

- A removal would delete reusable Redis Lua stock code.
- A removal would delete VNPAY gateway code needed by the payment story.
- A dependency appears required by current JPA, Redis, or React behavior.
- Validation cannot run and there is no clear replacement proof.

