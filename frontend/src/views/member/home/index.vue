<template>
  <div class="member-home">
    <HomeOverview />
    <HomePanel title="我的收藏" path="/member/collect">
      <GoodsItem v-for="item in collectGoods" :key="item.id" :goods="item" />
    </HomePanel>
    <HomePanel title="我的足迹" path="/member/history">
      <GoodsItem v-for="item in historyGoods" :key="item.id" :goods="item" />
    </HomePanel>
    <GoodsRelevant />
  </div>
</template>
<script>
import { ref } from "vue";
import HomeOverview from "./components/home-overview.vue";
import HomePanel from "./components/home-panel";
import GoodsRelevant from "@/views/goods/components/goods-relevant";
import GoodsItem from "@/views/category/components/goods-item";
import { findCollect, findHistory } from "@/api/member";

export default {
  name: "MemberHome",
  components: {
    HomeOverview,
    HomePanel,
    GoodsRelevant,
    GoodsItem,
  },
  setup() {
    const collectGoods = ref([]);
    findCollect({
      page: 1,
      pageSize: 4,
    }).then((data) => {
      collectGoods.value = data.result.items;
    }).catch(() => {
      collectGoods.value = [];
    });

    const historyGoods = ref([]);
    findHistory({
      page: 1,
      pageSize: 4,
    }).then((data) => {
      historyGoods.value = data.result.items;
    }).catch(() => {
      historyGoods.value = [];
    });

    return { collectGoods, historyGoods };
  },
};
</script>
<style scoped lang="less">
:deep(.xtx-carousel) .carousel-btn.prev {
  left: 5px;
}
:deep(.xtx-carousel) .carousel-btn.next {
  right: 5px;
}
</style>
