# Event Ticket Booking Overview

## Status

Accepted as planned product direction from `SPEC.MD` on 2026-05-22.

No implementation claim is made by this document. Existing code must be checked
against these contracts in story-sized implementation work.

## Goal

Build a simple event ticket booking system that is suitable for a Java/Spring
CV project. The system should be easy to run locally, easy to explain in an
interview, and strong enough to demonstrate:

- Multi-module DDD-style backend structure.
- React frontend integration.
- Spring Security authentication and authorization.
- MySQL persistence and transaction handling.
- Redis refresh tokens, cache, and stock state.
- Redis Lua atomic stock deduction.
- Mock payment and VNPAY sandbox payment.

## Users

The product supports two roles:

- `USER`: registers, logs in, browses events, places orders, pays, views own
  orders, and cancels pending own orders.
- `ADMIN`: manages events, ticket types, stock, and orders.

## In Scope

- Email/password registration and login.
- Google OAuth2 login.
- JWT access token and opaque refresh token.
- Public event list and event detail.
- Admin event and ticket type management.
- Order placement using `ticketTypeId` and quantity.
- Redis Lua stock reservation with MySQL consistency checks.
- Pending order cancellation with stock restore.
- Mock payment.
- VNPAY sandbox payment.
- React + Vite frontend pages for public, auth, user, and admin flows.
- Local demo stack with backend, frontend, MySQL, and Redis.

## Out Of Scope

The required product should remove these from the application runtime and
implementation story scope unless later accepted as optional documentation:

- Kafka.
- RabbitMQ.
- Prometheus.
- Grafana.
- ELK.
- Resilience4j demo endpoints.
- Employee sign-in demo.
- API key secure demo.
- Monthly sharded order tables.
- JMeter and k6 benchmark artifacts.

## Success Criteria

- A recruiter can run the project locally with only MySQL and Redis as required
  infrastructure.
- A user can register or log in, browse active events, order tickets, pay, and
  view order history.
- An admin can manage events, ticket types, stock, and orders.
- The Redis Lua stock flow is documented and tested.
- API docs and README explain the DDD modules and key technical choices.

