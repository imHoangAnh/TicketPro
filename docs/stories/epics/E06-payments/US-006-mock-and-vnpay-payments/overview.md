# US-006 Mock and VNPAY Payments

## Status

implemented

## Intake Classification

- Type: spec slice
- Lane: high-risk
- Reason: introduces external provider behavior (VNPAY sandbox), payment API endpoints, order status transitions, authorization rules, and public API contracts with weak existing proof.

## Goal

Implement the backend E06 slice so authenticated users can pay for `PENDING` orders through mock payment or VNPAY sandbox. The system creates payment records using the accepted `Payment` entity and `payments` table schema, transitions order status to `PAID` or `PAYMENT_FAILED`, and provides a VNPAY callback endpoint for signature verification.

## Scope

- `POST /api/payments/{orderId}/mock-success`
- `POST /api/payments/{orderId}/vnpay`
- `GET /api/payments/vnpay/callback`
- Payment record creation using accepted `Payment` entity with `PaymentStatus` enum
- Mock payment: instant `INIT` → `SUCCESS` with `MOCK` method
- VNPAY: `INIT` → `PENDING` with signed URL, then callback moves to `SUCCESS` or `FAILED`
- Order status transitions: `PENDING` → `PAID` or `PAYMENT_FAILED`
- Ownership/ADMIN authorization on payment endpoints
- VNPAY config externalized to `application.yml`
- Rewrite payment application service to use accepted `Payment` entity and `PaymentRecordRepository`
- Rewrite `VnPayGatewayServiceImpl` to use externalized config and accepted domain types
- Backend application and controller tests

## Out Of Scope

- React payment and checkout pages, owned by E07.
- Payment refund or partial payment flows.
- Real VNPAY production credentials.
- Order expiration timer or scheduled payment timeout.
- Legacy `/payment/**` controller rewrite (remains restricted to ADMIN until fully replaced).

## Affected Product Docs

- `docs/product/payments.md`
- `docs/product/api.md`
- `docs/product/domain-model.md`

## Affected Users

- `USER`: can pay for own orders.
- `ADMIN`: can trigger mock payment for any order.
