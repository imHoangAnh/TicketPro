# Execution Plan

## Goal

Implement mock payment and VNPAY sandbox payment flows using the accepted `Payment` entity and `/api/payments` API contract.

## Scope

In scope:

- Rewrite `PaymentAppService` to use accepted `Payment` entity and `PaymentRecordRepository`.
- Create VNPAY gateway adapter with externalized config.
- Create new `PaymentController` at `/api/payments` with authenticated endpoints.
- Add VNPAY callback endpoint with signature verification.
- Add VNPAY config properties to `application.yml`.
- Update `SecurityConfig` for VNPAY callback (public endpoint).
- Application service tests for mock and VNPAY flows.
- MockMvc controller tests for routes, auth, and ownership.

Out of scope:

- Frontend payment pages (E07).
- Legacy `/payment/**` controller removal (keep restricted to ADMIN).
- Payment refunds or expiration.

## Risk Classification

Risk flags:

- External systems (VNPAY sandbox).
- Public contracts (new payment API).
- Authorization (ownership checks).
- Data model (payment records via accepted entity).
- Existing behavior (replaces legacy payment flow).

Hard gates:

- External provider behavior (VNPAY signature).

## Work Phases

1. Add VNPAY config properties to `application.yml`.
2. Rewrite VNPAY gateway adapter to use `Payment` entity and externalized config.
3. Rewrite `PaymentAppService` with mock and VNPAY flows using accepted entities.
4. Create new `PaymentController` at `/api/payments` with mock, VNPAY init, and callback endpoints.
5. Update `SecurityConfig` to permit VNPAY callback without authentication.
6. Add application tests for payment service.
7. Add MockMvc tests for payment controller.
8. Run Maven validation and update `docs/TEST_MATRIX.md`.

## Stop Conditions

Pause for human confirmation if:

- E06 requires a schema migration beyond the accepted payments table.
- VNPAY signature verification cannot be tested without a live sandbox account.
- Validation requirements must be weakened.
- Payment behavior needs to modify order cancellation or stock logic from E05.
