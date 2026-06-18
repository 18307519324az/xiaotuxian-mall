<template>
  <div class="xtx-similar-page">
    <div class="container">
      <XtxBread>
        <XtxBreadItem to="/">首页</XtxBreadItem>
        <XtxBreadItem>相似商品</XtxBreadItem>
      </XtxBread>
      <!-- 标题 -->
      <div class="title">相似商品</div>
      <!-- 加载中 -->
      <div v-if="loading" class="loading">
        <div class="loading-icon"></div>
        <p>加载中...</p>
      </div>
      <!-- 错误状态 -->
      <div v-else-if="error" class="empty-state">
        <p class="empty-icon">&#9888;</p>
        <p class="empty-text">相似商品加载失败，请稍后重试</p>
        <XtxButton @click="loadSimilarGoods" type="primary">重新加载</XtxButton>
      </div>
      <!-- 空状态 -->
      <div v-else-if="goodsList.length === 0" class="empty-state">
        <p class="empty-icon">&#128722;</p>
        <p class="empty-text">暂无相似商品</p>
      </div>
      <!-- 商品列表 -->
      <div v-else class="goods-list">
        <div
          v-for="item in goodsList"
          :key="item.id"
          class="goods-item"
          @click="$router.push('/product/' + item.id)"
        >
          <div class="image">
            <img :src="item.picture" :alt="item.name" />
          </div>
          <p class="name ellipsis-2">{{ item.name }}</p>
          <p class="price">&yen;{{ item.price }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from "vue";
import { useRoute } from "vue-router";
import { findRelGoods } from "@/api/goods";
export default {
  name: "SimilarPage",
  setup() {
    const route = useRoute();
    const goodsList = ref([]);
    const loading = ref(true);
    const error = ref(false);

    const loadSimilarGoods = async () => {
      const goodsId = route.params.id;
      if (!goodsId) {
        loading.value = false;
        error.value = false;
        return;
      }
      loading.value = true;
      error.value = false;
      try {
        const data = await findRelGoods(goodsId);
        goodsList.value = data.result || [];
      } catch (e) {
        console.error("相似商品加载失败", e);
        error.value = true;
        goodsList.value = [];
      } finally {
        loading.value = false;
      }
    };

    loadSimilarGoods();

    return { goodsList, loading, error, loadSimilarGoods };
  },
};
</script>

<style scoped lang="less">
.xtx-similar-page {
  .title {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin: 20px 0;
    padding-left: 10px;
    border-left: 4px solid @xtxColor;
  }
  .loading {
    text-align: center;
    padding: 100px 0;
    color: #999;
    .loading-icon {
      display: inline-block;
      width: 40px;
      height: 40px;
      border: 4px solid #eee;
      border-top-color: @xtxColor;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  }
  .empty-state {
    text-align: center;
    padding: 100px 0;
    color: #999;
    .empty-icon {
      font-size: 48px;
      margin-bottom: 20px;
    }
    .empty-text {
      font-size: 18px;
      margin-bottom: 20px;
    }
  }
  .goods-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    padding: 20px 0;
    .goods-item {
      width: calc((100% - 60px) / 4);
      background: #fff;
      border: 1px solid #f5f5f5;
      border-radius: 4px;
      padding: 15px;
      cursor: pointer;
      transition: all 0.3s;
      box-sizing: border-box;
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border-color: @xtxColor;
      }
      .image {
        width: 100%;
        padding-bottom: 100%;
        position: relative;
        overflow: hidden;
        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      .name {
        margin-top: 10px;
        font-size: 14px;
        color: #333;
        height: 40px;
        line-height: 20px;
        overflow: hidden;
      }
      .price {
        margin-top: 8px;
        font-size: 18px;
        color: @priceColor;
        font-weight: bold;
      }
    }
  }
}
</style>
