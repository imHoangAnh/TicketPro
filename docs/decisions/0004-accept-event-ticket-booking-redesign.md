# 0004 Accept Event Ticket Booking Redesign

Date: 2026-05-22

## Status

Accepted

## Context

The user supplied `SPEC.MD` asking to redesign the current project into a clean
event ticket booking CV project for a fresher Java/Spring position. The spec
keeps the existing multi-module DDD backend shape and React frontend, but
changes the product contract, removes non-goal infrastructure, and adds
security, order, stock, payment, and frontend expectations.

The request touches auth, authorization, data model, external providers, public
API contracts, frontend behavior, existing code, and missing validation proof,
so it enters the high-risk lane.

## Decision

Accept `SPEC.MD` as source input for the first buildout and derive living
product contracts under `docs/product/`.

The target product is an event ticket booking system with:

- Java/Spring multi-module DDD-style backend.
- React + Vite frontend.
- MySQL persistence.
- Redis refresh tokens, cache, and stock.
- Redis Lua atomic stock deduction.
- Email/password auth, Google OAuth2, USER and ADMIN roles.
- Mock payment and VNPAY sandbox payment.
- Local runtime that requires only MySQL and Redis.

Kafka, RabbitMQ, Prometheus, Grafana, ELK, demo endpoints, monthly sharded order
tables, and benchmark artifacts are not part of the required product runtime.

## Alternatives Considered

1. Implement directly from `SPEC.MD`. Rejected because the spec is too broad and
   high-risk for a direct code pass.
2. Keep `SPEC.MD` as the living product plan. Rejected because Harness v0
   requires derived product docs, stories, validation matrix rows, and decisions
   as the operating surface.
3. Slice every implementation phase into full story packets immediately.
   Rejected to avoid creating stale packets before the first implementation
   stories are selected.

## Consequences

Positive:

- Product truth is now split into smaller current docs.
- The first implementation phases have high-risk story packets.
- Future agents can start from story, validation, and decision context rather
  than a monolithic spec.

Tradeoffs:

- Existing code is not yet aligned with the new contract.
- Later epics still need story packets before implementation.
- Validation commands and evidence remain planned until implementation work
  creates or runs them.

## Follow-Up

- Start with foundation cleanup and dependency alignment.
- Then implement the clean data model and migrations.
- Create high-risk story packets for auth, order stock, payments, and frontend
  before changing those areas.
