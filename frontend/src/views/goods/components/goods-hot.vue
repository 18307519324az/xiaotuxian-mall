<template>
  <div class="goods-hot">
    <h3>{{ title }}</h3>
    <div v-if="goodsList">
      <GoodsItem v-for="item in goodsList" :key="item.id" :goods="item" />
    </div>
  </div>
</template>
<script>
import { ref, computed, watch } from "vue";
import GoodsItem from "../../category/components/goods-item";
import { findHotGoods } from "@/api/goods";

export default {
  name: "GoodsHot",
  props: {
    type: {
      type: Number,
      default: 1,
    },
    goodsId: {
      type: String,
    },
  },
  components: {
    GoodsItem,
  },
  setup(props) {
    const titleObj = { 1: "24小时热销榜", 2: "周热销榜", 3: "总热销榜" };
    // 标题
    const title = computed(() => {
      return titleObj[props.type];
    });

    // 商品列表
    const goodsList = ref([]);

    const loadHotGoods = (id, type) => {
      if (!id) return;
      goodsList.value = [];
      findHotGoods({ id, type }).then((data) => {
        goodsList.value = data.result.map((item) => {
          item.tag = item.desc;
          return item;
        });
      });
    };

    // 初始加载
    loadHotGoods(props.goodsId, props.type);

    // 监听 goodsId 变化，路由切换时重新加载
    watch(
      () => props.goodsId,
      (newId) => {
        loadHotGoods(newId, props.type);
      }
    );

    return { title, goodsList };
  },
};
</script>
<style scoped lang="less">
.goods-hot {
  h3 {
    height: 70px;
    background: @helpColor;
    color: #fff;
    font-size: 18px;
    line-height: 70px;
    padding-left: 25px;
    margin-bottom: 10px;
    font-weight: normal;
  }
  ::v-deep .goods-item {
    background: #fff;
    width: 100%;
    margin-bottom: 10px;
    img {
      width: 200px;
      height: 200px;
    }
    p {
      margin: 0 10px;
    }
    &:hover {
      transform: none;
      box-shadow: none;
    }
  }
}
</style>
