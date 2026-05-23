# Validation

## Planned Proof

- `mvn -pl xxxx-controller -am test`
- `mvn test`
- `mvn package`
- `docker-compose -f environment\docker-compose-dev.yml config`

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Mock payment success, VNPAY payment URL generation, VNPAY callback success/failure, order not pending rejection, ownership check, ADMIN bypass, duplicate payment prevention, signature verification |
| Integration | MockMvc mock-success route auth, MockMvc VNPAY init route auth, MockMvc callback signature handling, forbidden cross-user access, ADMIN payment access |
| E2E | Not required for backend-first E06; frontend flows are E07 |
| Platform | Docker Compose config remains valid |
| Logs/Audit | Application logs include payment creation, status transitions, VNPAY callback results, and order status changes |

## Fixtures

- Active event with active ticket type and stock.
- USER principal with a PENDING order.
- USER principal that does not own the order.
- ADMIN principal for cross-user payment access.
- Order in PAID status for duplicate-payment rejection.
- VNPAY callback params with valid and invalid signatures.

## Commands

```text
mvn -pl xxxx-controller -am test
mvn test
mvn package
docker-compose -f environment\docker-compose-dev.yml config
```

## Acceptance Evidence

- pass: `mvn -pl xxxx-application -am test -Dtest=PaymentAppServiceImplTest` — 13 tests, 0 failures
- pass: `mvn -pl xxxx-controller -am test -Dtest=ApiPaymentControllerTest` — 11 tests, 0 failures
- pass: `mvn test` — 30 total tests, 0 failures, 0 errors
- pass: `mvn package` — BUILD SUCCESS
- pass: `docker-compose -f environment\docker-compose-dev.yml config` — config valid
