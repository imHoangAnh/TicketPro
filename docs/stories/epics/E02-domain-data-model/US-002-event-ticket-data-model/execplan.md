# Exec Plan

## Goal

Create the clean domain model and persistence schema required by the event
ticket booking product.

## Scope

In scope:

- Define domain entities and enums for users, roles, events, ticket types,
  orders, order items, and payments. Refresh tokens are Redis-only and are not
  represented as MySQL entities.
- Replace ambiguous ticket naming in new contracts with `eventId`,
  `ticketTypeId`, `orderId`, and `paymentId`.
- Create schema/migrations or deterministic init SQL for the accepted tables.
- Add seed data for one admin user and sample events if selected by the story.
- Add repository interfaces and persistence adapters needed by later stories.
- Define constraints for active events, positive prices, non-negative stock,
  and paid-order delete protection.

Out of scope:

- Email/password and Google OAuth2 flow implementation.
- Redis Lua stock deduction.
- Mock or VNPAY payment behavior.
- Full admin CRUD APIs.
- React page rebuild.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Data model.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Auth.
- Authorization.
- Data model.

Lane: high-risk.

## Work Phases

1. Inventory existing schema, entities, repositories, and SQL init files.
2. Confirm migration/init SQL strategy for local CV scope.
3. Define domain entities, enums, and repository interfaces.
4. Implement persistence tables and adapters.
5. Add deterministic seed data where required.
6. Add unit and integration proof for schema constraints and repository behavior.
7. Update docs, test matrix evidence, and decisions if schema direction changes.

## Stop Conditions

Pause for human confirmation if:

- The story requires destructive data migration rather than replacing local demo
  schema.
- Refresh token persistence is Redis-only instead of Redis plus MySQL.
- `stock_adjustment_log` must be deferred despite rollback reconciliation risk.
- A legacy table must be kept for compatibility with existing code.
