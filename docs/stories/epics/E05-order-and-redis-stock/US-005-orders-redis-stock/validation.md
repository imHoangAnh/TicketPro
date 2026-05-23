# Validation

## Planned Proof

- `mvn -pl xxxx-controller -am test`
- `mvn test`
- `mvn package`
- `docker-compose -f environment\docker-compose-dev.yml config`

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Redis stock success, cache miss warmup, out-of-stock response, inactive event/ticket validation, MySQL failure Redis rollback, cancel ownership/status rules |
| Integration | MockMvc route authentication, user-owned order access, forbidden cross-user access, admin order access/cancel |
| E2E | Not required for backend-first E05; frontend flows are E07 |
| Platform | Docker Compose config remains valid |
| Logs/Audit | Application logs include placement/cancel outcomes and Redis rollback failures |

## Fixtures

- Active event with active ticket type and stock.
- Inactive event or inactive ticket type for rejection tests.
- USER principal that owns an order.
- USER principal that does not own an order.
- ADMIN principal for cross-user order access.

## Evidence

- pass: `mvn -pl xxxx-controller -am test`
- pass: `mvn test`
- pass: `mvn package`
- pass: `docker-compose -f environment\docker-compose-dev.yml config`

Docker Compose emitted the known local warning:

```text
Error loading config file: open C:\Users\trhoa\.docker\config.json: Access is denied.
```

## Notes

- Application proof covers Redis Lua success, Redis cache miss warmup, Redis still-missing-after-warmup failure, out-of-stock response, inactive event rejection, MySQL stock conflict Redis rollback, own-user cancellation, and ADMIN cancellation.
- Controller proof covers authenticated order placement, out-of-stock conflict mapping, forbidden cross-user access, and ADMIN cancellation.
- Frontend order pages and payment state transitions remain outside E05.
