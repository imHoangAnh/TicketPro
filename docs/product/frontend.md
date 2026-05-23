# Frontend

## Stack

The frontend stays on React + Vite and is rebuilt around the accepted API
contract.

## Pages

Public:

- Home.
- Event list.
- Event detail.

Auth:

- Login.
- Register.
- OAuth2 callback.

User:

- My orders.
- Checkout/payment.
- Payment result.

Admin:

- Event management.
- Ticket type management.
- Order management.

## Auth Behavior

- Axios attaches the access token to authenticated requests.
- On `401`, the frontend attempts one refresh request and retries the original
  request once.
- Unauthenticated users are redirected to login for protected routes.
- Admin routes require the `ADMIN` role.
- Refresh token should be held in an HttpOnly cookie.
- Access token can be stored in memory or local storage for CV simplicity; the
  frontend story must choose one.
- Refresh and logout requests must include `X-Requested-With: XMLHttpRequest`
  because they mutate cookie-backed auth state.

## Non-Goals

- Frontend automated tests are optional for the initial CV scope.
- The UI should stay simple and support backend demonstration over visual polish.
