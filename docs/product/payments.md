# Payments

## Order Statuses

- `PENDING`
- `PAID`
- `CANCELLED`
- `PAYMENT_FAILED`
- `EXPIRED`

## Payment Statuses

- `INIT`
- `PENDING`
- `SUCCESS`
- `FAILED`

## Mock Payment

Endpoint:

```text
POST /api/payments/{orderId}/mock-success
```

Behavior:

1. Verify authenticated user owns the order or is `ADMIN`.
2. Verify order is `PENDING`.
3. Create payment record with method `MOCK`.
4. Mark payment `SUCCESS`.
5. Mark order `PAID`.

## VNPAY Sandbox

Endpoints:

```text
POST /api/payments/{orderId}/vnpay
GET  /api/payments/vnpay/callback
```

Behavior:

1. User requests a VNPAY payment URL.
2. Backend creates payment `PENDING`.
3. Backend builds signed VNPAY sandbox URL.
4. Frontend redirects user to VNPAY.
5. VNPAY redirects to the backend callback.
6. Backend verifies signature.
7. Payment becomes `SUCCESS` or `FAILED`.
8. Order becomes `PAID` or `PAYMENT_FAILED`.

## Config

VNPAY settings must come from environment or application config:

```text
vnpay.tmn-code
vnpay.secret-key
vnpay.pay-url
vnpay.return-url
```

Secrets must not be hardcoded.

