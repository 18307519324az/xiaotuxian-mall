# 结算页优惠券与礼品卡闭环

## 覆盖范围

结算页对优惠券与礼品卡的处理已经在以下环节形成闭环：

- 结算页选择与校验
- 同步下单请求
- 异步下单请求
- 支付页权益摘要
- 订单详情页权益摘要

当前前端主要文件：

- `src/views/member/pay/checkout.vue`
- `src/views/member/pay/index.vue`
- `src/views/member/order/components/detail-info.vue`
- `src/api/order.js`
- `src/api/member.js`

## 前端行为验证

已于 `2026-06-20` 在运行中的前端 `http://localhost:8086/#/member/checkout` 完成浏览器验收。

页面可见模块：

- 优惠券选择器
- 礼品卡选择器
- 库存提示行
- 金额汇总区

页面未出现以下异常文本：

- `NaN`
- `undefined`
- `null`

### 金额验证

实测值：

- 商品金额：`316.00`
- 在线支付运费：`0.00`
- 选择优惠券后的抵扣：`10.00`
- 选择礼品卡后的抵扣：`100.00`
- 优惠券与礼品卡同时生效后的应付：`206.00`
- 切换货到付款后的服务费：`5.00`
- 货到付款模式下最终应付：`211.00`

## 请求载荷验证

浏览器现场提交请求同时携带了权益参数，且没有发送 `undefined`。

抓取到的请求：

- URL：`http://localhost:8086/api/member/order`
- payload：

```json
{
  "deliveryTimeType": 1,
  "payType": 1,
  "payChannel": 1,
  "buyerMessage": "",
  "goods": [
    {
      "skuId": "1266175",
      "count": 4
    }
  ],
  "addressId": "address_001",
  "couponId": "coupon_63",
  "giftCardCode": "GIFT-2026-002"
}
```

运行说明：

- 当前前端开发代理会将 `8086` 上的 `/api/**` 转发到 `8099` Mock Service
- 因此浏览器抓包对应的是 Mock 同步下单路径
- 真实异步链路已单独在 `xtx-order-service` 上完成验证

## 真实服务权益验证

### 同步优惠券订单

- `orderNo=ORDER2026062054143087`
- `couponId=coupon_1`
- `discountAmount=20.00`
- `payMoney=240.00`
- `orderState=2`

### 异步优惠券订单

- `orderNo=ORDER2026062089652842`
- `couponId=coupon_1`
- `discountAmount=20.00`
- `payMoney=240.00`
- `orderState=2`

### 同步零元礼品卡订单

- `orderNo=ORDER2026062042352606`
- `giftCardCode=GIFT-2026-003`
- `giftCardAmount=75.00`
- `payMoney=0.00`
- `orderState=2`

### 异步零元礼品卡订单

- `orderNo=ORDER2026062094999087`
- `giftCardCode=GIFT-2026-003`
- `giftCardAmount=75.00`
- `payMoney=0.00`
- `orderState=2`

## 说明

- 早期示例礼品卡 `GIFT-2026-001` 余额已不足以覆盖零元支付验收
- 本轮零元复验改用新绑定礼品卡 `GIFT-2026-003`
- `checkout-stage1.vue` 仍未接入路由，当前生效页面仍是 `checkout.vue`
