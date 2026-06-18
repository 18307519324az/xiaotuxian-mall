<template>
  <div class="member-collect">
    <div class="member-nav">
      <span class="title">我的收藏</span>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="goodsList.length === 0" class="empty">
      <img src="@/assets/images/empty.png" alt="" />
      <p>暂无收藏的商品</p>
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
          <a href="javascript:;" class="del" @click.prevent="removeCollect(item.id)">取消收藏</a>
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
import { findCollect, deleteCollect } from "@/api/member";
import Message from "@/components/library/Message";

export default {
  name: "MemberCollect",
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
        const data = await findCollect({ page: page.value, pageSize: pageSize.value });
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

    const removeCollect = async (goodsId) => {
      try {
        await deleteCollect(goodsId);
        Message({ type: "success", text: "已取消收藏" });
        loadData();
      } catch (e) {
        Message({ type: "error", text: "取消收藏失败" });
      }
    };

    return { goodsList, loading, page, pageSize, total, totalPages, changePage, removeCollect };
  },
};
</script>

<style scoped lang="less">
.member-collect {
  background: #fff;
  min-height: 400px;
  .member-nav {
    padding: 20px;
    border-bottom: 1px solid #f5f5f5;
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
      position: relative;
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
      .del {
        display: none;
        position: absolute;
        bottom: 15px;
        right: 20px;
        color: @xtxColor;
        font-size: 12px;
      }
      &:hover .del {
        display: block;
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
