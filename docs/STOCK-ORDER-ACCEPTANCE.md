# 库存与订单验收清单

## 验收范围

本轮收口覆盖以下内容：

- 前端库存展示、排序与提交前校验
- 同步下单库存流转
- 异步下单 MQ 流转
- 优惠券与礼品卡支付字段
- 并发场景下的防超卖行为

## 前端验收

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| F1 | 分类页库存排序 | 可以按库存排序 |
| F2 | 分类页仅看有货 | 能隐藏零库存商品 |
| F3 | 商品详情库存展示 | 展示当前库存和库存状态 |
| F4 | 商品详情无货 SKU | 不允许购买 |
| F5 | 购物车校验 | 数量超库存时阻止结算 |
| F6 | 结算页校验 | 提交前再次校验可售库存 |

## 同步下单验收

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| S1 | `GET /member/order/token` | 返回有效 token |
| S2 | `POST /member/order` | 返回 `orderId`、`orderNo`、`payMoney` |
| S3 | 重复 token 提交 | 拒绝重复提交 |
| S4 | 缺失 token | 返回无效 token 错误 |
| S5 | Redis key 缺失 | 自动预热后重试 |
| S6 | 库存不足 | 返回库存不足错误 |
| S7 | 支付成功 | `lockedStock` 下降，`soldStock` 上升 |
| S8 | 取消订单 | `lockedStock` 下降，`availableStock` 恢复 |

## 异步下单验收

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| A1 | `POST /member/order/async` | 返回 `orderNo` 和 `PROCESSING` |
| A2 | `GET /member/order/process/{orderNo}` | 返回 `PROCESSING`、`SUCCESS` 或 `FAILED` |
| A3 | MQ 消费成功 | 处理状态变为 `SUCCESS` 且返回 `orderId` |
| A4 | MQ 发布失败 | Redis 预扣回滚，状态变为 `FAILED` |
| A5 | 消费失败 | Redis 预扣回滚，状态变为 `FAILED` |

## 权益验收

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| B1 | 同步优惠券订单 | `couponId` 和 `discountAmount` 落库 |
| B2 | 同步礼品卡订单 | `giftCardCode` 和 `giftCardAmount` 落库 |
| B3 | 异步优惠券订单 | `couponId` 和 `discountAmount` 落库 |
| B4 | 零元礼品卡订单 | `payMoney=0.00` 且订单进入已支付状态 |

## 并发验收

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| C1 | Redis 并发预扣 | 成功数不超过初始可售库存 |
| C2 | Redis 余量 | 不出现负数 |
| C3 | 数据库可售库存 | 不出现负数 |
| C4 | 库存守恒 | `totalStock = availableStock + lockedStock + soldStock` |

## 当前已验证证据

已于 `2026-06-20` 完成以下验证：

- 同步优惠券订单：`ORDER2026062054143087`
- 异步优惠券订单：`ORDER2026062089652842`
- 同步零元礼品卡订单：`ORDER2026062042352606`
- 异步零元礼品卡订单：`ORDER2026062094999087`

受控并发复验结果：

- `skuId=1027026`
- 受控基线：`20/10/1/9`
- 请求数：`20`
- 提交成功：`10`
- 提交失败：`10`
- 处理成功：`10`
- Redis：`10 -> 0 -> 10`
- 数据库：`20/10/1/9 -> 20/0/11/9 -> 20/10/1/9`
- 超卖：`否`

本地单元测试证据：

- `xtx-order-service`：`Tests run: 76, Failures: 0, Errors: 0`
- `xtx-inventory-service`：`Tests run: 14, Failures: 0, Errors: 0`
