# 库存与订单流程说明

## 当前阶段

当前发布包覆盖两条核心能力：

1. 前端库存展示、排序与提交前校验
2. Redis Lua 预扣库存加 RabbitMQ 异步下单链路

## 前端库存闭环

当前前端已经实现：

- 分类页支持按库存排序
- 分类页支持“仅看有货”过滤
- 商品详情页展示 `availableStock` 与库存状态
- 无货 SKU 不允许加入购物车
- 购物车校验 `count <= stock`
- 结算页提交前再次校验 `count <= availableStock`

## 同步下单流程

```text
GET /member/order/token
  -> 生成一次性提交 token

POST /member/order
  -> 校验 token
  -> 校验地址和商品
  -> 解析 couponId 与 giftCardCode
  -> 计算金额快照
  -> Redis Lua 预扣库存
  -> 数据库预占库存
  -> 持久化订单和订单商品
  -> 当 payMoney > 0 时创建支付单
  -> 零元支付订单自动确认
```

## 异步下单流程

```text
GET /member/order/token
  -> 生成一次性提交 token

POST /member/order/async
  -> 校验 token
  -> 校验地址和商品
  -> 解析 couponId 与 giftCardCode
  -> 计算金额快照
  -> Redis Lua 预扣库存
  -> 写入 PROCESSING 状态
  -> 发布 MQ 创建订单消息
  -> 立即返回 orderNo

consumer
  -> 异步创建订单
  -> 持久化处理结果
```

## 库存状态流转

数据库库存使用以下字段表示：

- `totalStock`
- `availableStock`
- `lockedStock`
- `soldStock`

状态变化规则：

- 预扣成功：`availableStock` 减少，`lockedStock` 增加
- 支付成功：`lockedStock` 减少，`soldStock` 增加
- 取消订单：`lockedStock` 减少，`availableStock` 回补
- 整体满足：`totalStock = availableStock + lockedStock + soldStock`

## 优惠券与礼品卡

同步下单和异步下单都会带上：

- `couponId`
- `giftCardCode`

结算阶段完成金额计算后，权益快照会写入订单：

- 优惠券抵扣金额
- 礼品卡抵扣金额
- 实付金额
- 货到付款手续费

支付页和订单详情页会回显这些字段。

## 相关文档

- [库存与订单验收清单](./STOCK-ORDER-ACCEPTANCE.md)
- [异步下单 E2E 验收报告](./ASYNC-ORDER-E2E-QA.md)
- [异步下单库存链路收口报告](./ASYNC-ORDER-STOCK-CLOSEOUT.md)
- [结算页优惠券与礼品卡闭环](./ORDER-BENEFIT-FLOW.md)
