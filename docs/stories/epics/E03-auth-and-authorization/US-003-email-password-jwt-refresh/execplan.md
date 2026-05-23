# Exec Plan

## Goal

Implement the first backend auth slice with email/password registration and
login, JWT access tokens, Redis-only refresh tokens, refresh rotation, logout,
`/me`, and role-based route authorization.

## Scope

In scope:

- Auth DTOs, controller, and application service.
- JWT provider.
- Redis refresh token repository.
- BCrypt password hashing.
- Spring Security JWT filter and route rules.
- Focused tests for auth behavior and authorization.
- Harness updates for story status and validation evidence.

Out of scope:

- Google OAuth2.
- Frontend auth pages.
- VNPAY, event, order, and payment flow changes.
- MySQL refresh token persistence.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Audit/security.
- Public contracts.
- Existing behavior.
- Weak proof.

Hard gates:

- Auth.
- Authorization.

Lane: high-risk.

## Work Phases

1. Add story packet and validation expectations.
2. Add auth commands, responses, and app service.
3. Add Redis refresh token store.
4. Add JWT provider and Spring Security enforcement.
5. Add tests.
6. Run validation.
7. Request code review.
8. Apply fixes and update Harness.

## Stop Conditions

Pause for human confirmation if:

- Refresh token storage changes away from Redis-only.
- The route authorization contract needs to be weakened.
- A DB-backed session table becomes necessary.
- Tests cannot cover refresh rotation or role rejection.
