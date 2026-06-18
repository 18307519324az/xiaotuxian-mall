<template>
  <div class="detail-action">
    <div class="state">
      <span class="iconfont" :class="[`icon-order-${orderStatus[order.orderState].name}`]"></span>
      <p>{{ orderStatus[order.orderState].label }}</p>
    </div>

    <div class="btn">
      <!-- 待付款 -->
      <template v-if="order.orderState === 1">
        <XtxButton @click="$router.push('/member/pay?orderId=' + order.id)" type="primary" size="small">立即付款</XtxButton>
        <XtxButton @click="onCancelOrder(order)" type="gray" size="small">取消订单</XtxButton>
      </template>
      <!-- 待发货 -->
      <template v-if="order.orderState === 2">
        <XtxButton type="primary" size="small" @click="onShipOrder(order)">模拟发货</XtxButton>
        <XtxButton @click="$router.push(`/member/checkout?orderId=${order.id}`)" type="plain" size="small"
          >再次购买</XtxButton
        >
      </template>
      <!-- 待收货 -->
      <template v-if="order.orderState === 3">
        <XtxButton type="primary" size="small" @click="onConfirmOrder(order)">确认收货</XtxButton>
        <XtxButton @click="$router.push(`/member/checkout?orderId=${order.id}`)" type="plain" size="small"
          >再次购买</XtxButton
        >
      </template>
      <!-- 待评价 -->
      <template v-if="order.orderState === 4">
        <XtxButton @click="$router.push(`/member/checkout?orderId=${order.id}`)" type="plain" size="small"
          >再次购买</XtxButton
        >
        <XtxButton type="plain" size="small">
          <RouterLink :to="'/member/review?orderId=' + order.id" style="color:inherit;text-decoration:none">评价商品</RouterLink>
        </XtxButton>
        <XtxButton type="gray" size="small">
          <RouterLink :to="'/member/after-sale?orderId=' + order.id" style="color:inherit;text-decoration:none">申请售后</RouterLink>
        </XtxButton>
      </template>
      <!-- 已完成 -->
      <template v-if="order.orderState === 5">
        <XtxButton @click="$router.push(`/member/checkout?orderId=${order.id}`)" type="plain" size="small"
          >再次购买</XtxButton
        >
        <XtxButton type="plain" size="small">
          <RouterLink :to="'/member/review?orderId=' + order.id" style="color:inherit;text-decoration:none">查看评价</RouterLink>
        </XtxButton>
        <XtxButton type="gray" size="small">
          <RouterLink :to="'/member/after-sale?orderId=' + order.id" style="color:inherit;text-decoration:none">申请售后</RouterLink>
        </XtxButton>
      </template>
      <!-- 已取消 -->
    </div>
    <Teleport to="#dailog">
      <OrderCancel ref="orderCancelCom" />
    </Teleport>
  </div>
</template>
<script>
import OrderCancel from "./order-cancel";
import { orderStatus } from "@/api/constants";
import { useCancelOrder, useConfirmOrder } from "../index.vue";
import { shipOrder, confirmOrder } from "@/api/order";
import Confirm from "@/components/library/Confirm";
import Message from "@/components/library/Message";
export default {
  name: "OrderDetailPage",
  props: {
    order: {
      type: Object,
      default: () => ({}),
    },
  },
  components: {
    OrderCancel,
  },
  setup(props) {
    const onShipOrder = (order) => {
      shipOrder(order.id).then(() => {
        Message({ type: "success", text: "模拟发货成功" });
        order.orderState = 3;
      }).catch(() => {
        Message({ type: "error", text: "模拟发货失败" });
      });
    };
    // 确认收货 - 使用详情页的 confirm + 本地状态更新
    const { onConfirmOrder: baseOnConfirm } = useConfirmOrder();
    const onConfirmOrder = (order) => {
      Confirm({ text: "您确认收到货吗？确认后货款将会打给卖家。" }).then(() => {
        confirmOrder(order.id).then(() => {
          Message({ text: "确认收货成功", type: "success" });
          // 更新本地状态，确保确认收货按钮消失
          if (order) order.orderState = 4;
        });
      });
    };
    return { orderStatus, onShipOrder, onConfirmOrder, ...useCancelOrder() };
  },
};
</script>
<style scoped lang="less">
.detail-action {
  height: 180px;
  width: 100%;
  display: flex;
  align-items: center;
  .state {
    width: 220px;
    text-align: center;
    .iconfont {
      font-size: 40px;
      color: @xtxColor;
    }
    p {
      font-size: 16px;
      color: #666;
      margin-bottom: 10px;
    }
  }
  .info {
    width: 240px;
    line-height: 30px;
    p {
      color: #999;
    }
  }
  .btn {
    flex: 1;
    text-align: right;
    margin-right: 100px;
    .xtx-button {
      margin-left: 20px;
    }
  }
}
</style>
