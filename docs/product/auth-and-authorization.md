# Auth And Authorization

## Security Model

The backend uses Spring Security with two roles:

- `USER`
- `ADMIN`

Access tokens are JWTs. Refresh tokens are random opaque tokens stored in Redis
and preferably sent as HttpOnly cookies.

## Access Token

- Short-lived JWT.
- Suggested lifetime: 15 minutes.
- Claims include user ID, email, and roles.

## Refresh Token

- Opaque random token, not JWT.
- Suggested lifetime: 7 to 30 days.
- Stored in Redis only.
- Not persisted in MySQL for the active product scope.
- Rotated on refresh.
- Invalidated on logout.
- Redis stores a hash of the refresh token, not the raw token.
- Refresh consumes the old token atomically before issuing a new token.

## Cookie Mutation Guard

Refresh and logout mutate cookie-backed auth state. They require:

```text
X-Requested-With: XMLHttpRequest
```

This keeps the initial CV scope simple while adding a browser-side guard for
credentialed cookie endpoints. The frontend must send this header when calling
`POST /api/auth/refresh` and `POST /api/auth/logout`.

## Email And Password Endpoints

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

## Google OAuth2 Endpoints

```text
GET /oauth2/authorization/google
GET /login/oauth2/code/google
```

## OAuth2 Behavior

1. The user clicks "Login with Google" in the frontend.
2. The backend redirects to Google authorization.
3. Google redirects to the backend callback.
4. The backend finds or creates a user by email.
5. The backend assigns default role `USER`.
6. The backend creates access token and refresh token.
7. The backend redirects to the frontend with a short one-time auth code.
8. The frontend exchanges the one-time code for tokens.

## Route Authorization

| Scope | Routes |
| --- | --- |
| Public | `GET /api/events`, `GET /api/events/{eventId}`, auth endpoints, OAuth2 endpoints |
| `USER` | `/api/orders/**`, `/api/payments/**` |
| `ADMIN` | `/api/admin/**` |

## Frontend Auth Behavior

- Access token may be stored in memory or local storage for CV simplicity.
- Refresh token should use an HttpOnly cookie.
- Axios attaches the access token.
- On `401`, the frontend calls refresh once with `X-Requested-With:
  XMLHttpRequest` and retries the original request.
- Protected routes redirect unauthenticated users to login.
- Admin routes require `ADMIN`.
