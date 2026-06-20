# XTX Inventory Service

## Scope

`xtx-inventory-service` owns SKU stock, reservation, release, confirm-deduction, Redis warmup, and Redis Lua pre-deduct.

Current runtime exposes:

- `GET /goods/stock/{skuId}`
- `POST /inventory/stocks/warmup`
- `GET /inventory/stocks/redis/{skuId}`
- `POST /inner/stocks/pre-deduct`
- `POST /inner/stocks/rollback`
- `POST /inner/stocks/reserve`
- `POST /inner/stocks/release/{orderNo}`
- `POST /inner/stocks/confirm/{orderNo}`

## Stock Model

The service keeps these fields consistent per SKU:

- `totalStock`
- `availableStock`
- `lockedStock`
- `soldStock`
- `version`

Invariant:

```text
totalStock = availableStock + lockedStock + soldStock
```

`version` is used as the optimistic-lock column for database reservation, release, and confirm-deduction updates.

## Redis Warmup

Redis stock keys use:

```text
xtx:stock:sku:{skuId}
```

Warmup request example:

```bash
curl -X POST http://localhost:8106/inventory/stocks/warmup \
  -H "Content-Type: application/json" \
  -d '{"skuIds":[1027026],"forceRefresh":true}'
```

Inspect Redis stock:

```bash
curl http://localhost:8106/inventory/stocks/redis/1027026
```

## Redis Lua Pre-Deduct

Lua scripts live in:

- `src/main/resources/lua/stock_pre_deduct.lua`
- `src/main/resources/lua/stock_rollback.lua`

Behavior:

1. Warm database stock into Redis on demand.
2. Pre-deduct Redis stock atomically by `requestId`.
3. Treat duplicate `requestId` as idempotent success.
4. Roll back previously deducted Redis stock when later SKU deduction fails.

## Database Reservation Flow

Normal path:

1. `reserveStocks(orderNo)` moves `availableStock -> lockedStock`
2. `confirmDeduction(orderNo)` moves `lockedStock -> soldStock`
3. `releaseStocks(orderNo)` moves `lockedStock -> availableStock`

Reservation idempotency depends on `stock_reservation(order_no, sku_id)` unique constraint.

## Verification

Local verification used on 2026-06-20:

```bash
mvn -pl xtx-services/xtx-inventory-service -am test
```

Result:

```text
Tests run: 14, Failures: 0, Errors: 0
```

Covered areas include:

- Redis warmup
- Lua pre-deduct success / insufficient stock / duplicate request
- Redis rollback idempotency
- concurrency non-oversell test (`InventoryConcurrentDeductTest`)
