# Overview

## Current Behavior

Before this story, the backend had the intended multi-module shape, but the
current code did not fully enforce the DDD dependency rule in
`docs/ARCHITECTURE.md`.

Known drift from the current review:

- `xxxx-application` depends on `xxxx-infrastructure`.
- Application services inject concrete infrastructure adapters such as
  `VnPayGatewayServiceImpl`.
- Application cache services import Redis infrastructure classes directly.
- Some domain classes and services still carry Spring/JPA coupling. That is
  architectural debt, but this story's first required cleanup is the direct
  `xxxx-application` dependency on `xxxx-infrastructure`.
- Accepted `/api/...` behavior still coexists with legacy `/ticket`, `/order`,
  and `/payment` surfaces.
- Frontend and API contract drift exists around admin order endpoints, but this
  story only addresses backend DDD architecture boundaries.

Backend tests passed before implementation, so the immediate risk was
architectural erosion rather than a known backend compile failure.

## Implementation Summary

US-008 removed the direct `xxxx-application -> xxxx-infrastructure` dependency
and introduced application-owned ports for:

- key/value cache operations,
- Redis stock scripts,
- distributed locks,
- payment gateway URL/signature handling.

Infrastructure now implements those contracts. `xxxx-start` depends on
`xxxx-infrastructure` as the runtime composition root, while application tests
mock application ports instead of infrastructure classes. A JUnit architecture
test now checks the Maven dependency and forbidden Java imports.

## Target Behavior

The backend follows the DDD-flavoured module direction documented in
`docs/ARCHITECTURE.md`:

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

Application services orchestrate use cases through domain services and
repository interfaces. Infrastructure implements adapters for MySQL, Redis,
JWT, OAuth2, VNPAY, and other external concerns. The immediate target is to
remove application imports of infrastructure implementations and make the
dependency direction enforceable.

The refactor must preserve accepted product behavior for auth, events, orders,
stock, payments, and current backend API routes while making module boundaries
enforceable.

## Affected Users

- Developer maintaining the backend.
- Future agent implementing story work.
- Recruiter or interviewer evaluating the DDD design.
- End users indirectly, because the refactor must not change booking behavior.

## Affected Product Docs

- `docs/ARCHITECTURE.md`
- `docs/product/domain-model.md`
- `docs/product/api.md`
- `docs/product/orders-and-stock.md`
- `docs/product/payments.md`
- `docs/product/validation.md`
- `docs/TEST_MATRIX.md`

## Non-Goals

- Do not redesign the product domain or add new user-facing behavior.
- Do not rebuild the frontend in this story.
- Do not add a new persistence technology.
- Do not remove accepted `/api/...` contracts.
- Do not perform destructive database migration without a separate approved
  migration story.
- Do not weaken auth, authorization, payment, or stock validation.
