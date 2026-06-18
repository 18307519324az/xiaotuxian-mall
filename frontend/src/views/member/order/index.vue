<template>
  <div class="member-order-page">
    <XtxTabs v-model="activeName" @tab-click="tabClick">
      <XtxTabsPanel v-for="item in orderStatus" :key="item.name" :label="item.label" :name="item.name" />
    </XtxTabs>
    <!-- 订单列表 -->
    <div class="order-list">
      <div v-if="loading" class="loading"></div>
      <div class="none" v-if="!loading && orderList.length === 0">
        <p class="none-icon">&#128230;</p>
        <p>暂无订单数据</p>
        <p class="none-hint">快去挑选心仪的商品吧</p>
      </div>
      <OrderItem
        v-for="item in orderList"
        :key="item.id"
        :order="item"
        @on-cancel="onCancelOrder(item)"
        @on-delete="handlerDelete(item)"
        @on-confirm="onConfirmOrder(item)"
        @on-logistics="handlerLogistics(item)"
        @on-ship="onShipOrder(item)"
      />
    </div>
    <!-- 分页组件 -->
    <XtxPagination
      v-if="total > 0"
      :current-page="reqParams.page"
      :page-size="reqParams.pageSize"
      :total="total"
      @current-change="reqParams.page = $event"
    />

    <!-- 对于像：对话框，消息提示，确认提示，通知组件 适合使用传送门 Teleport -->
    <Teleport to="#dailog">
      <OrderCancel ref="orderCancelCom" />
    </Teleport>

    <!-- 查看物理组件 -->
    <Teleport to="#dailog">
      <OrderLogistics ref="orderLogisticsCom" />
    </Teleport>
  </div>
</template>
<script>
import { ref, reactive, watch } from "vue";
import OrderItem from "./components/order-item";
import OrderCancel from "./components/order-cancel";
import Confirm from "@/components/library/Confirm";
import Message from "@/components/library/Message";
import { confirmOrder, deleteOrder, findOrderList, shipOrder } from "@/api/order";
import { orderStatus } from "@/api/constants";
import OrderLogistics from "./components/order-logistics";

// 封装逻辑-取消订单
export const useCancelOrder = () => {
  const orderCancelCom = ref(null);
  const onCancelOrder = (item) => {
    // item 就是你要取消的订单
    orderCancelCom.value.open(item);
  };
  return { onCancelOrder, orderCancelCom };
};

// 封装逻辑-确认收货
export const useConfirmOrder = (getOrderList) => {
  const onConfirmOrder = (item) => {
    // item 就是你要确认收货的订单
    Confirm({ text: "您确认收到货吗？确认后货款将会打给卖家。" }).then(() => {
      confirmOrder(item.id).then(() => {
        Message({ text: "确认收货成功", type: "success" });
        // 重新拉取订单列表，确保 tab 归类正确
        if (getOrderList) getOrderList();
      });
    });
  };
  return { onConfirmOrder };
};

// 封装逻辑-模拟发货
export const useShipOrder = (getOrderList) => {
  const onShipOrder = (item) => {
    shipOrder(item.id).then(() => {
      Message({ type: "success", text: "模拟发货成功" });
      if (getOrderList) getOrderList();
    }).catch(() => {
      Message({ type: "error", text: "模拟发货失败" });
    });
  };
  return { onShipOrder };
};

// 封装逻辑-查看物流逻辑
export const useLogistics = () => {
  const orderLogisticsCom = ref(null);
  const handlerLogistics = (order) => {
    orderLogisticsCom.value.open(order);
  };
  return { handlerLogistics, orderLogisticsCom };
};

export default {
  name: "MemberOrder",
  components: { OrderItem, OrderCancel, OrderLogistics },
  setup() {
    const activeName = ref("all");

    // 查询订单参数
    const reqParams = reactive({
      page: 1,
      pageSize: 5,
      orderState: 0,
    });
    // 订单列表
    const orderList = ref([]);
    const loading = ref(true);
    const total = ref(0);

    const getOrderList = () => {
      loading.value = true;
      findOrderList(reqParams).then((data) => {
        orderList.value = data.result.items;
        total.value = data.result.counts;
        loading.value = false;
      }).catch(() => {
        loading.value = false;
        orderList.value = [];
        total.value = 0;
      });
    };

    watch(
      reqParams,
      () => {
        getOrderList();
      },
      { immediate: true }
    );

    const tabClick = ({ index }) => {
      reqParams.page = 1;
      reqParams.orderState = index;
    };

    // 删除订单
    const handlerDelete = (order) => {
      Confirm({ text: "亲，您确认删除该订单吗？" })
        .then(() => {
          deleteOrder(order.id).then(() => {
            Message({ type: "success", text: "删除成功" });
            getOrderList();
          });
        })
        .catch((err) => {
          console.log(err);
        });
    };

    return {
      activeName,
      orderStatus,
      orderList,
      tabClick,
      loading,
      total,
      reqParams,
      handlerDelete,
      ...useCancelOrder(),
      ...useConfirmOrder(getOrderList),
      ...useShipOrder(getOrderList),
      ...useLogistics(),
    };
  },
};
</script>

<style scoped lang="less">
.member-order {
  height: 100%;
  background: #fff;
}
.order-list {
  background: #fff;
  padding: 20px;
  position: relative;
  min-height: 400px;
}
.loading {
  height: 100%;
  width: 100%;
  position: absolute;
  left: 0;
  top: 0;
  background: rgba(255, 255, 255, 0.9) url(../../../assets/images/loading.gif) no-repeat center;
}
.none {
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  .none-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  .none-hint {
    font-size: 13px;
    color: #bbb;
    margin-top: 6px;
  }
}
</style>
