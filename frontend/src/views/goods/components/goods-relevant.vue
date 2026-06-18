<template>
  <div class="goods-relevant">
    <div class="header">
      <i class="icon" />
      <span class="title">{{ goodsId ? "同类商品推荐" : "猜你喜欢" }}</span>
    </div>
    <!-- 此处使用改造后的xtx-carousel.vue -->
    <XtxCarousel :sliders="sliders" />
  </div>
</template>

<script>
import { ref, watch } from "vue";
import { findRelGoods } from "@/api/goods";
export default {
  // 同类推荐，猜你喜欢
  name: "GoodsRelevant",
  props: {
    goodsId: {
      type: String,
      default: "",
    },
  },
  setup(props) {
    // 最终需要的数据是 sliders 提供给轮播图使用
    // 数据结构：sliders = [[4个],[4个],[4个],[4个]]
    const sliders = ref([]);

    const loadRelevantGoods = (id) => {
      sliders.value = [];
      // 无 goodsId 时请求全站热销（"猜你喜欢"）
      findRelGoods(id || '').then((data) => {
        // data.result 商品列表，数据结构 [16个]
        // 将data.result数据赋值给sliders数据，保证数据结构正确
        const pageSize = 4;
        const pageCount = Math.ceil(data.result.length / pageSize);
        const newSliders = [];
        for (let i = 0; i < pageCount; i++) {
          newSliders.push(data.result.slice(pageSize * i, pageSize * (i + 1)));
        }
        sliders.value = newSliders;
      });
    };

    // 初始加载
    loadRelevantGoods(props.goodsId);

    // 监听 goodsId 变化，路由切换时重新加载
    watch(
      () => props.goodsId,
      (newId) => {
        loadRelevantGoods(newId);
      }
    );

    return { sliders };
  },
};
</script>

<style scoped lang="less">
.goods-relevant {
  background: #fff;
  min-height: 460px;
  margin-top: 20px;
  .header {
    height: 80px;
    line-height: 80px;
    padding: 0 20px;
    .title {
      font-size: 20px;
      padding-left: 10px;
    }
    .icon {
      width: 16px;
      height: 16px;
      display: inline-block;
      border-top: 4px solid @xtxColor;
      border-right: 4px solid @xtxColor;
      box-sizing: border-box;
      position: relative;
      transform: rotate(45deg);
      &::before {
        content: "";
        width: 10px;
        height: 10px;
        position: absolute;
        left: 0;
        top: 2px;
        background: lighten(@xtxColor, 40%);
      }
    }
  }
}
:deep(.xtx-carousel) {
  height: 380px;
  .carousel {
    &-indicator {
      bottom: 30px;
      span {
        &.active {
          background: @xtxColor;
        }
      }
    }
    &-btn {
      top: 110px;
      opacity: 1;
      background: rgba(0, 0, 0, 0);
      color: #ddd;
      i {
        font-size: 30px;
      }
    }
  }
}
</style>
