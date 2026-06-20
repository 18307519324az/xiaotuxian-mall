# XTX Order Service

## Scope

`xtx-order-service` owns preview, submit, async submit, process-status query, cancel, pay-confirm, timeout release, and stock compensation orchestration.

Current runtime exposes:

- `GET /member/order/token`
- `GET /member/order/pre`
- `GET /member/order/repurchase/{orderId}`
- `POST /member/order`
- `POST /member/order/async`
- `GET /member/order/process/{orderNo}`
- `GET /member/order/{orderId}`
- `GET /member/order/no/{orderNo}`
- `POST /member/order/{orderNo}/pay`
- `PUT /member/order/{orderNo}/cancel`

## Sync Order

Sync submit path:

1. validate address and goods
2. validate and consume order token
3. compute coupon / gift-card benefits
4. Redis pre-deduct stock
5. create order + order goods
6. reserve database stock
7. create payment order when `payMoney > 0`
8. zero-pay orders confirm stock immediately

## Async Order

Async submit path:

1. `GET /member/order/token`
2. `POST /member/order/async`
3. Redis Lua pre-deduct
4. write process status `PROCESSING`
5. publish MQ create-order message
6. consumer creates order and reserves stock
7. `GET /member/order/process/{orderNo}` returns `SUCCESS` or `FAILED`

MQ components:

- exchange: `xtx.order.exchange`
- create queue: `xtx.order.create.queue`
- dead queue: `xtx.order.dead.queue`
- compensation queue: `xtx.stock.compensation.queue`

## Idempotency

Protection layers:

- order token to stop repeated submit clicks
- `requestId` for Redis pre-deduct idempotency
- `orderNo` unique index in `order_info`
- `orderNo + skuId` unique index in `stock_reservation`
- consumer duplicate handling in MQ flow

## Payment / Cancel / Timeout

- payment confirm calls inventory confirm-deduction
- cancel calls database reservation release and Redis rollback
- timeout job scans pending-pay orders older than 30 minutes and cancels them
- compensation job retries failed stock rollback tasks

## Benefits

Coupon and gift-card fields are persisted in `order_info` and returned in order detail:

- `couponId`
- `couponName`
- `couponType`
- `discountGoodsAmount`
- `discountFreightAmount`
- `discountAmount`
- `giftCardCode`
- `giftCardAmount`

## Verification

Local verification used on 2026-06-20:

```bash
mvn -pl xtx-services/xtx-order-service -am test
```

Result:

```text
Tests run: 76, Failures: 0, Errors: 0
```

Covered areas include:

- token service
- async submit
- process-status query
- MQ producer
- MQ consumer
- dead-letter consumer
- compensation service
- benefit calculation
