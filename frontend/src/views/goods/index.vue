<template>
  <!-- 商品详情 — 加载中 -->
  <div class="xtx-goods-page-loading" v-if="loading">
    <div class="container">
      <div class="skeleton-bread">
        <XtxSkeleton width="200px" height="18px" animated />
      </div>
      <div class="skeleton-body">
        <div class="skeleton-media">
          <XtxSkeleton width="480px" height="480px" animated />
        </div>
        <div class="skeleton-spec">
          <XtxSkeleton width="70%" height="32px" animated />
          <div style="margin-top: 20px;">
            <XtxSkeleton width="40%" height="24px" animated />
          </div>
          <div style="margin-top: 16px;">
            <XtxSkeleton width="60%" height="50px" animated />
          </div>
          <div style="margin-top: 30px;">
            <XtxSkeleton width="160px" height="50px" animated />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 商品详情 — 加载失败 -->
  <div class="xtx-goods-page-error" v-else-if="loadError">
    <div class="container">
      <div class="error-state">
        <p class="error-icon">&#9888;</p>
        <p class="error-text">商品加载失败，请稍后重试</p>
        <XtxButton @click="retryLoad" type="primary">重新加载</XtxButton>
      </div>
    </div>
  </div>

  <!-- 商品详情 — 正常展示 -->
  <div :key="route.params.id" class="xtx-goods-page" v-else-if="goods">
    <div class="container">
      <!-- 面包屑 -->
      <XtxBread>
        <XtxBreadItem to="/">首页</XtxBreadItem>
        <XtxBreadItem :to="'/category/' + goods.categories[0].id">{{ goods.categories[0].name }}</XtxBreadItem>
        <XtxBreadItem :to="'/category/sub/' + goods.categories[1].id">{{ goods.categories[1].name }}</XtxBreadItem>
        <XtxBreadItem>{{ goods.name }}</XtxBreadItem>
      </XtxBread>
      <!-- 商品信息 -->
      <div class="goods-info">
        <div class="media">
          <GoodsImage :images="goods.mainPictures" />
          <GoodsSales :goods="goods" />
        </div>
        <div class="spec">
          <!-- 名字区组件 -->
          <GoodsName :goods="goods" />
          <!-- 规格组件 -->
          <GoodsSku :key="goods.id" :goods="goods" @change="changeSku" />
          <!-- 数量选择组件 -->
          <XtxNumbox :key="goods.id" label="数量" v-model="num" :max="goods.inventory" />
          <!-- 按钮组件 -->
          <XtxButton @click="insertCart()" type="primary" style="margin-top: 20px">加入购物车</XtxButton>
        </div>
      </div>
      <!-- 商品推荐 -->
      <GoodsRelevant :goodsId="goods.id" />

      <!-- 商品详情 -->
      <div class="goods-footer">
        <div class="goods-article">
          <div class="goods-tabs">
            <!-- 商品+评价 -->
            <GoodsTabs />
          </div>
          <div class="goods-warn">
            <!-- 注意事项 -->
            <GoodsWarn />
          </div>
        </div>
        <!-- 24热榜+专题推荐 -->
        <div class="goods-aside">
          <GoodsHot :goodsId="goods.id" :type="1" />
          <GoodsHot :goodsId="goods.id" :type="2" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GoodsRelevant from "./components/goods-relevant";
import GoodsImage from "./components/goods-image";
import GoodsSales from "./components/goods-sales";
import GoodsName from "./components/goods-name";
import GoodsSku from "./components/goods-sku";
import GoodsTabs from "./components/goods-tabs";
import GoodsHot from "./components/goods-hot";
import GoodsWarn from "./components/goods-warn";
import { provide, ref, watch, computed } from "vue";
import { findGoods } from "@/api/product";
import { addHistory } from "@/api/member";
import { useRoute } from "vue-router";
import { useStore } from "vuex";
import Message from "@/components/library/Message";
export default {
  name: "XtxGoodsPage",
  components: {
    GoodsRelevant,
    GoodsImage,
    GoodsSales,
    GoodsName,
    GoodsSku,
    GoodsTabs,
    GoodsHot,
    GoodsWarn,
  },
  setup() {
    const goods = ref(null);
    const route = useRoute();
    const currSku = ref(null);
    const num = ref(1);
    const loading = ref(true);
    const loadError = ref(false);

    const loadGoods = async (id) => {
      if (!id || `/product/${id}` !== route.path) return;

      // 强制重置全部状态，触发 v-if 隐藏，让子组件完全卸载
      goods.value = null;
      currSku.value = null;
      num.value = 1;
      loading.value = true;
      loadError.value = false;

      // 回到页面顶部
      window.scrollTo(0, 0);

      // 加载新商品数据
      try {
        const data = await findGoods(id);
        goods.value = data.result;
        loading.value = false;
        // 静默记录足迹
        addHistory(id).catch(() => {});
      } catch (e) {
        console.error('Failed to load goods:', e);
        loading.value = false;
        loadError.value = true;
      }
    };

    watch(
      () => route.params.id,
      async (id) => {
        await loadGoods(id);
      },
      { immediate: true }
    );

    const retryLoad = () => {
      loadGoods(route.params.id);
    };

    const changeSku = (sku) => {
      if (sku.skuId) {
        goods.value.price = sku.price;
        goods.value.oldPrice = sku.oldPrice;
        goods.value.inventory = sku.inventory;
      }
      currSku.value = sku;
    };

    provide("goods", goods);

    // Provide activeGoodsTab for child components (goods-sales, goods-tabs)
    // Provide switchTab callback for Options API components (Vue 3.3+ auto-unwraps refs in Options API inject)
    const activeGoodsTab = ref("detail");
    provide("activeGoodsTab", activeGoodsTab);
    provide("switchTab", (name) => { activeGoodsTab.value = name; });

    const store = useStore();
    const insertCart = () => {
      if (currSku.value && currSku.value.skuId) {
        const { skuId, specsText: attrsText, inventory: stock } = currSku.value;
        const { id, name, price, mainPictures, pictures } = goods.value;
        const pic = mainPictures ? mainPictures[0] : (pictures ? pictures[0] : "");
        store
          .dispatch("cart/insertCart", {
            skuId,
            attrsText,
            stock,
            id,
            name,
            price,
            nowPrice: price,
            picture: pic,
            selected: true,
            isEffective: true,
            count: num.value,
          })
          .then(() => {
            Message({ type: "success", text: "加入购物车成功" });
          });
      } else {
        Message({ text: "请选择完整规格" });
      }
    };

    return { goods, loading, loadError, changeSku, num, insertCart, route, retryLoad };
  },
};
</script>

<style scoped lang="less">
.goods-info {
  min-height: 600px;
  background: #fff;
  display: flex;
  .media {
    width: 580px;
    height: 600px;
    padding: 30px 50px;
  }
  .spec {
    flex: 1;
    padding: 30px 30px 30px 0;
  }
}
.goods-footer {
  display: flex;
  margin-top: 20px;
  .goods-article {
    width: 940px;
    margin-right: 20px;
  }
  .goods-aside {
    width: 280px;
    min-height: 1000px;
  }
}
.goods-tabs {
  min-height: 600px;
  background: #fff;
}
.goods-warn {
  min-height: 600px;
  background: #fff;
  margin-top: 20px;
}

/* 加载骨架屏 */
.xtx-goods-page-loading {
  min-height: 600px;
  .container {
    background: #fff;
    padding: 20px;
  }
}
.skeleton-bread {
  padding: 10px 0;
  margin-bottom: 20px;
}
.skeleton-body {
  display: flex;
  gap: 40px;
}
.skeleton-media {
  width: 580px;
  height: 600px;
}
.skeleton-spec {
  flex: 1;
}

/* 错误状态 */
.xtx-goods-page-error {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  .error-state {
    text-align: center;
    background: #fff;
    padding: 60px;
    border-radius: 4px;
    .error-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }
    .error-text {
      font-size: 16px;
      color: #666;
      margin-bottom: 20px;
    }
  }
}
</style>
