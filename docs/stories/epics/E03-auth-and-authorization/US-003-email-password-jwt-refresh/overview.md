# Overview

## Current Behavior

The backend currently permits all requests in Spring Security. User, role, and
user-role tables exist, but there is no accepted email/password auth flow, JWT
access token handling, Redis refresh token lifecycle, logout, or authenticated
identity endpoint.

## Target Behavior

The backend supports the first auth slice for the event ticket booking product:

- Users can register with email/password.
- Users can log in and receive a short-lived JWT access token.
- Refresh tokens are opaque random tokens stored in Redis only.
- Refresh tokens are sent as HttpOnly cookies and rotated on refresh.
- Refresh rotation consumes the old Redis token atomically.
- Logout invalidates the current refresh token.
- `GET /api/auth/me` returns the current authenticated user and roles.
- `USER` is assigned by default at registration.
- `ADMIN` role checks are enforced for `/api/admin/**`.
- Refresh and logout require `X-Requested-With: XMLHttpRequest`.

## Affected Users

- `USER` registering, logging in, refreshing sessions, and calling protected
  user APIs.
- `ADMIN` using admin-only APIs.
- Developer implementing later event, order, payment, and frontend stories.

## Affected Product Docs

- `docs/product/auth-and-authorization.md`
- `docs/product/api.md`
- `docs/product/frontend.md`
- `docs/product/validation.md`

## Non-Goals

- Do not implement Google OAuth2 in this slice.
- Do not rebuild frontend auth pages in this slice.
- Do not add a MySQL `refresh_tokens` table.
- Do not implement access-token blacklist or global logout-all-sessions.
