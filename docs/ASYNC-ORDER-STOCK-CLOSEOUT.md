# 异步下单库存链路收口报告

## 分支

- `feature/async-order-stock`

## 本轮收口范围

- Redis Lua 预扣与回滚
- 订单 token 与重复提交保护
- 同步下单与异步下单创建
- RabbitMQ 创建订单消息的发布与消费
- 优惠券与礼品卡权益持久化
- 取消订单与支付成功后的库存守恒
- 受控真实环境并发复验

## 运行组件

- 库存预热：`POST /inventory/stocks/warmup`
- Redis 库存探针：`GET /inventory/stocks/redis/{skuId}`
- 同步下单接口：`POST /member/order`
- 异步下单接口：`POST /member/order/async`
- 异步状态接口：`GET /member/order/process/{orderNo}`
- 支付确认：`POST /member/order/{orderNo}/pay`
- 取消订单：`PUT /member/order/{orderNo}/cancel`

## MQ 组件

- exchange：`xtx.order.exchange`
- 创建队列：`xtx.order.create.queue`
- 死信队列：`xtx.order.dead.queue`
- 补偿队列：`xtx.stock.compensation.queue`
- producer：`OrderCreateMessageProducer`
- consumer：`OrderCreateMessageConsumer`
- 死信消费者：`OrderDeadLetterConsumer`

RabbitMQ 本地运行端口为 `5672`。

本地 `order-service` 日志中已记录：

- producer 发布 `ORDER2026062089652842`
- consumer 创建 `orderId=86`
- producer 发布 `ORDER2026062094999087`
- consumer 创建 `orderId=91`

上述日志可以证明运行中的真实服务已完成发布和消费。

## 受控并发复验

已于 `2026-06-20` 针对 `skuId=1027026` 进行受控真实环境复验。

### 受控基线

- 数据库强制为 `total=20, available=10, locked=1, sold=9`
- Redis 预热为 `10`

### 并发执行结果

- 请求数：`20`
- 提交成功：`10`
- 提交失败：`10`
- 处理成功：`10`
- 处理失败：`0`

成功订单：

- `ORDER2026062030330517`
- `ORDER2026062064348715`
- `ORDER2026062042706069`
- `ORDER2026062061299080`
- `ORDER2026062095461159`
- `ORDER2026062014174400`
- `ORDER2026062057521323`
- `ORDER2026062098255840`
- `ORDER2026062004178655`
- `ORDER2026062035098164`

### 库存变化

- Redis：`10 -> 0 -> 10`
- 异步创建后数据库：`20/0/11/9`
- 取消清理后数据库：`20/10/1/9`

### 恢复

复验完成后已恢复原始库存快照：

- 原始数据库：`1000/972/1/27`
- 原始 Redis：`972`
- 恢复后数据库：`1000/972/1/27`
- 恢复后 Redis：`972`

### 结论

- 超卖：`否`
- Redis 负库存：`否`
- 数据库负库存：`否`
- 库存守恒：`totalStock = availableStock + lockedStock + soldStock`

## 已验证的权益订单

- 同步优惠券订单：`ORDER2026062054143087`
- 异步优惠券订单：`ORDER2026062089652842`
- 同步零元礼品卡订单：`ORDER2026062042352606`
- 异步零元礼品卡订单：`ORDER2026062094999087`

## 相关文档

- `docs/ASYNC-ORDER-E2E-QA.md`
- `docs/ORDER-BENEFIT-FLOW.md`
- `docs/STOCK-ORDER-ACCEPTANCE.md`
- `docs/STOCK-ORDER-FLOW.md`
