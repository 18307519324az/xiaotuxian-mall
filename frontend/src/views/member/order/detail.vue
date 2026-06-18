<template>
  <div class="member-order-detail">
    <div v-if="loading" class="loading-detail">
      <XtxSkeleton animated bg="#e4e4e4" width="100%" height="40px" style="margin-bottom:10px" />
      <XtxSkeleton animated bg="#e4e4e4" width="100%" height="180px" style="margin-bottom:10px" />
      <XtxSkeleton animated bg="#e4e4e4" width="100%" height="60px" style="margin-bottom:10px" />
      <XtxSkeleton animated bg="#e4e4e4" width="100%" height="120px" />
    </div>
    <template v-else-if="order">
      <DetailAction :order="order" />
      <DetailSteps :order="order" />
      <Suspense>
        <template #default>
          <DetailLogistics v-if="[3, 4, 5].includes(order.orderState)" :order="order" />
        </template>
        <template #fallback>
          <div class="loading-placeholder">loading...</div>
        </template>
      </Suspense>
      <DetailInfo :order="order" />
    </template>
    <div v-else class="loading-error">
      <p>订单加载失败，请稍后重试</p>
    </div>
  </div>
</template>
<script>
import { ref } from "vue";
import { findOrderDetail } from "@/api/order";
import { useRoute } from "vue-router";
import DetailAction from "./components/detail-action";
import DetailSteps from "./components/detail-steps";
import DetailLogistics from "./components/detail-logistics";
import DetailInfo from "./components/detail-info";

export default {
  name: "OrderDetailPage",
  components: { DetailAction, DetailSteps, DetailLogistics, DetailInfo },
  setup() {
    const order = ref(null);
    const loading = ref(true);
    const route = useRoute();
    findOrderDetail(route.params.id).then((data) => {
      order.value = data.result;
    }).catch(() => {
      // Error state - order stays null, loading becomes false
    }).finally(() => {
      loading.value = false;
    });
    return { order, loading };
  },
};
</script>

<style scoped lang="less">
.member-order-detail {
  background: #fff;
  height: 100%;
}
.loading {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 30px;
  background-color: #f5f5f5;
  margin: 30px 50px 0;
}
.loading-detail {
  padding: 30px;
}
.loading-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #999;
  font-size: 14px;
}
</style>
