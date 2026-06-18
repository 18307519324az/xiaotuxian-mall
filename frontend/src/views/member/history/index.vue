<template>
  <div class="member-history">
    <div class="member-nav">
      <span class="title">我的足迹</span>
      <XtxButton @click="clearAll()" v-if="goodsList.length > 0" type="plain" size="small" style="margin-left: auto">清空</XtxButton>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="goodsList.length === 0" class="empty">
      <img src="@/assets/images/empty.png" alt="" />
      <p>暂无浏览记录</p>
    </div>
    <template v-else>
      <div class="goods-list">
        <RouterLink
          v-for="item in goodsList"
          :key="item.id"
          :to="`/product/${item.id}`"
          class="goods-item"
        >
          <img v-img-error :src="item.picture" alt="" />
          <p class="name ellipsis-2">{{ item.name }}</p>
          <p class="price">&yen;{{ item.price }}</p>
        </RouterLink>
      </div>
      <XtxPagination
        v-if="totalPages > 1"
        :currentPage="page"
        :pageSize="pageSize"
        :total="total"
        @current-change="changePage"
      />
    </template>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { findHistory, clearHistory } from "@/api/member";
import Message from "@/components/library/Message";

export default {
  name: "MemberHistory",
  setup() {
    const goodsList = ref([]);
    const loading = ref(true);
    const page = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const totalPages = ref(0);

    const loadData = async () => {
      loading.value = true;
      try {
        const data = await findHistory({ page: page.value, pageSize: pageSize.value });
        goodsList.value = data.result.items || [];
        total.value = data.result.counts || 0;
        totalPages.value = Math.ceil(total.value / pageSize.value);
      } catch (e) {
        goodsList.value = [];
      } finally {
        loading.value = false;
      }
    };

    onMounted(loadData);

    const changePage = (newPage) => {
      page.value = newPage;
      loadData();
    };

    const clearAll = async () => {
      try {
        await clearHistory();
        Message({ type: "success", text: "已清空足迹" });
        loadData();
      } catch (e) {
        Message({ type: "error", text: "清空失败" });
      }
    };

    return { goodsList, loading, page, pageSize, total, totalPages, changePage, clearAll };
  },
};
</script>

<style scoped lang="less">
.member-history {
  background: #fff;
  min-height: 400px;
  .member-nav {
    padding: 20px;
    border-bottom: 1px solid #f5f5f5;
    display: flex;
    align-items: center;
    .title {
      font-size: 18px;
      font-weight: 500;
    }
  }
  .goods-list {
    display: flex;
    flex-wrap: wrap;
    padding: 10px;
    .goods-item {
      display: block;
      width: 220px;
      padding: 15px 20px;
      text-align: center;
      &:hover {
        .hoverShadow();
      }
      img {
        width: 160px;
        height: 160px;
      }
      .name {
        font-size: 14px;
        padding-top: 10px;
        text-align: left;
      }
      .price {
        color: @priceColor;
        font-size: 18px;
        padding-top: 5px;
        text-align: left;
      }
    }
  }
  .loading {
    text-align: center;
    padding: 100px 0;
    color: #999;
  }
  .empty {
    text-align: center;
    padding: 80px 0;
    img {
      width: 120px;
    }
    p {
      color: #999;
      margin-top: 15px;
    }
  }
}
</style>
