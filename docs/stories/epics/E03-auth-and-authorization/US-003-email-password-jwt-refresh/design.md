# Design

## Domain Model

The story uses the accepted `User`, `Role`, and `UserRole` entities. New users
are created with provider `LOCAL`, BCrypt password hashes, enabled status, and
the default `USER` role.

Refresh tokens are not domain entities. They are opaque session credentials
stored in Redis through a repository contract.

## Application Flow

Register:

1. Validate email, password, and full name.
2. Reject duplicate email.
3. Hash password with BCrypt.
4. Save user.
5. Assign default role `USER`.
6. Issue access token and refresh token.

Login:

1. Load enabled user by email.
2. Verify BCrypt password.
3. Load roles.
4. Issue access token and refresh token.

Refresh:

1. Read refresh token from HttpOnly cookie.
2. Hash token and atomically consume the Redis session.
3. Reject the request if the token has already been consumed.
4. Create and store a new refresh token.
5. Issue a new access token.

Logout:

1. Read refresh token from cookie.
2. Delete the matching Redis entry.
3. Clear refresh cookie.

## Interface Contract

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

Access tokens are returned in JSON and sent on later requests with
`Authorization: Bearer <token>`. Refresh tokens are set in an HttpOnly cookie
named `refresh_token` for local development. Refresh and logout requests must
include `X-Requested-With: XMLHttpRequest`.

## Data Model

No MySQL table is added. Redis keys use:

```text
AUTH:REFRESH:{tokenHash}
```

The value contains user ID, email, roles, issued time, and expiry time. The key
TTL matches the refresh-token lifetime.

The refresh operation consumes this key with Redis `GETDEL` semantics through
Spring Data Redis `getAndDelete`, so concurrent refresh calls with the same old
token can only produce one new session.

## UI / Platform Impact

Frontend pages are not implemented in this story. Existing CORS allows
`http://localhost:5173` with credentials so the future frontend can receive and
send the refresh cookie.

## Observability

The application logs register, login, refresh, and logout outcomes without
logging raw access tokens, raw refresh tokens, or passwords.

## Alternatives Considered

1. Store refresh tokens in MySQL. Rejected because the accepted product
   contract says Redis-only refresh tokens.
2. Use JWT refresh tokens. Rejected because opaque tokens are easier to revoke
   and rotate through Redis.
3. Implement Google OAuth2 immediately. Deferred to keep the first auth slice
   testable and bounded.
