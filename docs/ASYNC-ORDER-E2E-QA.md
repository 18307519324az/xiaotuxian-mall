# 异步下单 E2E 验收报告

已于 `2026-06-20` 在 Windows 11 本地真实服务环境完成验收，运行组件包括 MySQL 8、Redis 和 RabbitMQ。

## 运行环境

| 组件 | 端口 | 状态 |
| --- | --- | --- |
| goods-service | 8105 | 可访问 |
| inventory-service | 8106 | 可访问 |
| cart-service | 8107 | 可访问 |
| order-service | 8108 | 可访问 |
| payment-service | 8109 | 可访问 |
| member-service | 8110 | 可访问 |
| RabbitMQ | 5672 | 可访问 |

## 已验证的异步订单

### 异步优惠券订单

请求参数：

- `addressId=1`
- `skuId=1027026`
- `count=4`
- `couponId=coupon_1`

结果：

- `orderNo=ORDER2026062089652842`
- `requestId=ec674d49-b44a-4335-bf31-8d5f0ddb9620`
- 处理状态：`PROCESSING -> SUCCESS`
- `orderId=86`
- `orderState=2`
- `discountAmount=20.00`
- `payMoney=240.00`

### 异步礼品卡订单

此前使用的礼品卡样例余额已不足以覆盖零元支付验收，因此本轮改用新的已绑定卡重新验证。

请求参数：

- `addressId=1`
- `skuId=1027026`
- `count=1`
- `giftCardCode=GIFT-2026-003`

结果：

- `orderNo=ORDER2026062094999087`
- `requestId=6316a198-822f-4643-826f-dba62a9fc16d`
- 处理状态：`PROCESSING -> SUCCESS`
- `orderId=91`
- `orderState=2`
- `giftCardAmount=75.00`
- `payMoney=0.00`

## MQ 消费证据

本地 `order-service` 运行日志显示：

- producer 已发布 `ORDER2026062089652842`
- consumer 已创建 `orderId=86`
- producer 已发布 `ORDER2026062094999087`
- consumer 已创建 `orderId=91`

这说明异步链路已经覆盖“发布消息 -> 消费落单 -> 状态写回”的完整过程。

## 前端状态感知

前端在异步下单场景下不会直接把下单结果视为完成，而是先进入处理中状态。页面层需要处理：

- 提交成功后拿到 `orderNo`
- 先展示 `PROCESSING`
- 继续轮询处理状态
- 根据 `SUCCESS` 或 `FAILED` 决定后续页面跳转

这也是异步下单和普通同步下单在前端体验上的核心差异。

## 验收边界

本次 E2E 验收重点关注的是：

- 请求是否真正进入 RabbitMQ 链路
- 消费端是否成功创建真实订单
- 异步状态接口是否可查询
- 优惠券与礼品卡字段是否正确落库

本次不把它包装成生产级压测报告，重点是证明完整链路已跑通。

## 风险与补充说明

异步下单链路中最容易出问题的点通常包括：

- token 被重复提交
- MQ 发布失败
- MQ 消费失败
- 库存预扣后未及时回滚
- 前端轮询超时或页面状态未更新

本轮验收文档保留这些关注点，是为了让公开读者能快速理解该链路的风险面。

## 验收结论

- 异步下单接口会先返回处理中状态
- RabbitMQ 消费成功后，订单可在真实服务中查询
- 优惠券与礼品卡字段会随异步订单正确持久化
- 零元礼品卡订单可直接进入已支付状态流转
