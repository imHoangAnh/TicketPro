# Execution Plan

1. Add accepted order command/DTO models and update the order application service interface.
2. Implement Redis stock reservation, cache-miss warmup, MySQL conditional stock update, order item creation, and rollback behavior.
3. Add authenticated `/api/orders` controller endpoints and remove E05 dependence on caller-supplied `userId`.
4. Add application tests for stock success, cache miss, out-of-stock, inactive event/ticket type, rollback, and cancel rules.
5. Add MockMvc tests for route authentication, ownership, and stable status behavior.
6. Run Maven validation and update `docs/TEST_MATRIX.md` with evidence.

## Stop Conditions

Pause for human confirmation if:

- E05 requires a schema migration beyond the accepted US-002 tables.
- Validation requirements must be weakened.
- Redis rollback cannot be made explicit enough without adding a reconciliation table.
- Payment behavior must be pulled into the story to complete order placement.
