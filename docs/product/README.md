# Product Docs

These files are the living product contract derived from `SPEC.MD` on
2026-05-22. Treat `SPEC.MD` as source input and historical context; use these
smaller files as the current operating surface for future story work.

## Current Contracts

| File | Purpose |
| --- | --- |
| `spec-intake.md` | Intake classification, candidate epics, validation shape, and open decisions |
| `overview.md` | Product goal, scope, users, non-goals, and success criteria |
| `domain-model.md` | Product entities, naming contract, statuses, and business rules |
| `auth-and-authorization.md` | Token model, OAuth2 behavior, and route authorization |
| `api.md` | Public, auth, user, and admin API groups |
| `events-and-ticket-types.md` | Event browsing, admin event/ticket management, and cache rules |
| `orders-and-stock.md` | Order placement, Redis Lua stock reservation, and cancellation |
| `payments.md` | Mock payment and VNPAY sandbox behavior |
| `frontend.md` | React pages and frontend auth behavior |
| `local-development.md` | Required local services, module responsibilities, and docs |
| `validation.md` | Expected test cases and validation ladder |

## Update Rule

When behavior changes:

1. Update the affected product doc.
2. Update or create the story packet.
3. Update `docs/TEST_MATRIX.md`.
4. Record a decision if the change affects architecture, scope, risk, or a
   previously settled product rule.
