<template>
  <div class="xtx-pay-page">
    <div class="container">
      <XtxBread>
        <XtxBreadItem to="/">首页</XtxBreadItem>
        <XtxBreadItem to="/cart">购物车</XtxBreadItem>
        <XtxBreadItem>支付订单</XtxBreadItem>
      </XtxBread>
      <!-- 付款信息 -->
      <div class="pay-info" v-if="order">
        <span class="icon iconfont icon-queren2"></span>
        <div class="tip">
          <p>订单提交成功！请尽快完成支付。</p>
          <p>订单编号：{{ order.orderNo }}</p>
          <p>订单状态：{{ orderStatusLabel }}</p>
          <p v-if="order.countdown > -1">
            支付还剩 <span>{{ timeText }}</span
            >, 超时后将取消订单
          </p>
          <p v-else>订单已经超时</p>
        </div>
        <div class="amount">
          <span>应付总额：</span>
          <span>¥{{ order.payMoney }}</span>
        </div>
      </div>
      <!-- 付款方式 -->
      <div class="pay-type">
        <p class="head">选择以下支付方式付款</p>
        <div class="item">
          <p>支付平台</p>
          <a class="btn wx" :class="{ active: selectedChannel === 'wechat' }" href="javascript:;" @click="selectChannel('wechat')"></a>
          <a class="btn alipay" :class="{ active: selectedChannel === 'alipay' }" href="javascript:;" @click="selectChannel('alipay')"></a>
          <a class="btn bank" :class="{ active: selectedChannel === 'bank' }" href="javascript:;" @click="selectChannel('bank')">银行卡</a>
        </div>
      </div>
      <!-- 确认支付 -->
      <div class="submit" v-if="order">
        <XtxButton @click="confirmPayFn" type="primary" :loading="paying" :disabled="paying">确认支付</XtxButton>
      </div>
    </div>
  </div>
</template>
<script>
import { useRoute, useRouter } from "vue-router";
import { findOrderDetail, payOrder } from "@/api/order";
import { orderStatus } from "@/api/constants";
import { ref, computed } from "vue";
import { usePayTime } from "@/hooks";
import Message from "@/components/library/Message";

export default {
  name: "XtxPayPage",
  setup() {
    // 根据地址栏ID发请求获取订单数据（兼容 orderId 和 id）
    const route = useRoute();
    const router = useRouter();
    const orderId = route.query.orderId || route.query.id;
    const order = ref(null);
    findOrderDetail(orderId).then((data) => {
      order.value = data.result;
      // 后端提供 countdown 倒计时秒数
      if (data.result.countdown > -1) {
        start(data.result.countdown);
      }
    });

    // 倒计时工具函数
    const { start, timeText } = usePayTime();

    // 订单状态标签
    const orderStatusLabel = computed(() => {
      if (!order.value) return "";
      const state = order.value.orderState;
      return orderStatus[state] ? orderStatus[state].label : "未知状态";
    });

    // 支付平台选择
    const selectedChannel = ref("wechat");
    const selectChannel = (channel) => {
      selectedChannel.value = channel;
    };

    // 确认支付
    const paying = ref(false);
    const confirmPayFn = () => {
      if (paying.value) return;
      if (!selectedChannel.value) {
        return Message({ text: "请选择支付平台" });
      }
      paying.value = true;
      payOrder({ orderId: orderId, payChannel: selectedChannel.value })
        .then((data) => {
          Message({ type: "success", text: "支付成功" });
          router.push(`/member/order/${orderId}`);
        })
        .catch((err) => {
          const msg = err.response?.data?.message || "支付失败，请重试";
          Message({ type: "error", text: msg });
          paying.value = false;
        });
    };

    return {
      order,
      orderId,
      timeText,
      orderStatusLabel,
      selectedChannel,
      selectChannel,
      paying,
      confirmPayFn,
    };
  },
};
</script>
<style scoped lang="less">
.pay-info {
  background: #fff;
  display: flex;
  align-items: center;
  height: 240px;
  padding: 0 80px;
  .icon {
    font-size: 80px;
    color: #1dc779;
  }
  .tip {
    padding-left: 10px;
    flex: 1;
    p {
      &:first-child {
        font-size: 20px;
        margin-bottom: 5px;
      }
      &:last-child {
        color: #999;
        font-size: 16px;
      }
    }
  }
  .amount {
    span {
      &:first-child {
        font-size: 16px;
        color: #999;
      }
      &:last-child {
        color: @priceColor;
        font-size: 20px;
      }
    }
  }
}
.pay-type {
  margin-top: 20px;
  background-color: #fff;
  padding-bottom: 70px;
  p {
    line-height: 70px;
    height: 70px;
    padding-left: 30px;
    font-size: 16px;
    &.head {
      border-bottom: 1px solid #f5f5f5;
    }
  }
  .btn {
    width: 150px;
    height: 50px;
    border: 1px solid #e4e4e4;
    text-align: center;
    line-height: 48px;
    margin-left: 30px;
    color: #666666;
    display: inline-block;
    &.active,
    &:hover {
      border-color: @xtxColor;
    }
    &.alipay {
      background: url(https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/7b6b02396368c9314528c0bbd85a2e06.png) no-repeat
        center / contain;
    }
    &.wx {
      background: url(https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/c66f98cff8649bd5ba722c2e8067c6ca.jpg) no-repeat
        center / contain;
    }
    &.bank {
      background: #f5f5f5 url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 24'%3E%3Crect width='40' height='24' rx='2' fill='%23ddd'/%3E%3Crect x='4' y='4' width='32' height='3' fill='%23999'/%3E%3Crect x='4' y='11' width='18' height='2' fill='%23999'/%3E%3Crect x='4' y='16' width='12' height='2' fill='%23999'/%3E%3C/svg%3E") no-repeat center 10px;
      background-size: 40px 24px;
      padding-top: 28px;
      line-height: 22px;
      font-size: 14px;
      color: #666;
      &.active,
      &:hover {
        border-color: @xtxColor;
      }
    }
  }
}
.submit {
  background: #fff;
  text-align: right;
  padding: 20px 30px 40px;
}
</style>
