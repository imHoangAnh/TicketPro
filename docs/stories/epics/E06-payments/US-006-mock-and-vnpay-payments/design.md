# Design

## API Shape

Authenticated user endpoints:

- `POST /api/payments/{orderId}/mock-success` — instant mock payment.
- `POST /api/payments/{orderId}/vnpay` — returns VNPAY redirect URL.

Public callback endpoint:

- `GET /api/payments/vnpay/callback` — VNPAY redirect-back with query parameters.

## Application Rules

### Mock Payment

1. Verify authenticated user owns the order or is `ADMIN`.
2. Verify order is `PENDING`.
3. Create `Payment` record with method `MOCK`, status `INIT`.
4. Immediately transition payment to `SUCCESS`.
5. Transition order to `PAID`.
6. Return success response with payment ID.

### VNPAY Payment

1. Verify authenticated user owns the order or is `ADMIN`.
2. Verify order is `PENDING`.
3. Create `Payment` record with method `VNPAY`, status `PENDING`.
4. Build signed VNPAY sandbox URL using externalized config.
5. Store payment URL on the payment record.
6. Return VNPAY redirect URL to caller.

### VNPAY Callback

1. VNPAY redirects user browser to `GET /api/payments/vnpay/callback` with signed query params.
2. Backend verifies HMAC-SHA512 signature against configured secret key.
3. If signature invalid, return failure.
4. Look up payment record by `vnp_TxnRef`.
5. If `vnp_ResponseCode == "00"`, mark payment `SUCCESS` and order `PAID`.
6. Otherwise, mark payment `FAILED` and order `PAYMENT_FAILED`.
7. Redirect user to frontend order result page (configurable return URL).

## Interface Contract

`POST /api/payments/{orderId}/mock-success` returns:
- `200` with payment details on success.
- `404` if order not found.
- `403` if user does not own the order and is not admin.
- `409` if order is not `PENDING`.

`POST /api/payments/{orderId}/vnpay` returns:
- `200` with `{ paymentUrl: "https://sandbox.vnpayment.vn/..." }` on success.
- Same error codes as mock for ownership/status checks.

`GET /api/payments/vnpay/callback` returns:
- `302` redirect to frontend with payment result query params.
- `400` if signature verification fails.

## Data Model

Uses existing accepted `payments` table from US-002 init SQL. The `Payment` entity (JPA `@Entity` on `payments` table) replaces the legacy `PaymentTransaction` / `PaymentTransactionDO` / `payment_transaction` table approach. The accepted schema stores `status` as enum string (`INIT`, `PENDING`, `SUCCESS`, `FAILED`).

No schema migration is needed — the `payments` table already exists in init SQL.

## Observability

Application logs should include:
- Payment creation with `userId`, `orderId`, `paymentId`, `method`.
- Payment status transitions.
- VNPAY callback signature verification results.
- Order status transitions triggered by payment.

## Alternatives Considered

1. Keep legacy `PaymentTransaction` entity and `payment_transaction` table. Rejected because the accepted schema uses `payments` table with the `Payment` entity already defined in US-002.
2. Use Redis lock for payment idempotency. Deferred — single-user payment-per-order with DB unique constraint provides sufficient demo-level idempotency.
3. Async VNPAY IPN (server-to-server) callback. Deferred — the sandbox uses browser redirect callback which is simpler and sufficient for CV demo scope.
