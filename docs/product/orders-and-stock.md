# Orders And Stock

## Place Order

Endpoint:

```text
POST /api/orders
```

Request:

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

## Order Placement Flow

1. Controller parses authenticated user, `ticketTypeId`, and quantity.
2. Application service validates event and ticket type are active.
3. Application service calls Redis Lua to atomically decrement stock.
4. If the Redis stock key is missing, the service loads MySQL stock, warms the
   Redis key, and retries the decrement.
5. If Redis reports insufficient stock, the request returns out of stock.
6. If Redis reserves stock, the service creates order and order item records and
   performs a conditional MySQL stock update in the transaction.
7. If the MySQL update fails, the service increments Redis stock back.
8. If Redis rollback fails, the service logs the error and either writes a
   reconciliation record or documents an admin follow-up warning.

## Redis Stock Key

```text
TICKET_TYPE:{ticketTypeId}:STOCK
```

## Lua Decrement Contract

- Return `-1` when the key does not exist.
- Return `0` when stock is not enough.
- Return `1` when decrement succeeds.

## MySQL Safety Update

```sql
UPDATE ticket_types
SET stock_available = stock_available - :quantity
WHERE id = :ticketTypeId
  AND stock_available >= :quantity
```

## Cancel Order

Endpoint:

```text
PUT /api/orders/{orderId}/cancel
```

Rules:

- `USER` can cancel only own order.
- `ADMIN` can cancel any order if business rules allow.
- Only `PENDING` orders can be cancelled.
- Cancelling restores MySQL stock and Redis stock.

Flow:

1. Load order.
2. Verify ownership or `ADMIN` role.
3. Verify status is `PENDING`.
4. Update order to `CANCELLED`.
5. Increment `ticket_types.stock_available`.
6. Increment Redis stock key.

