<template>
  <div class="home-new">
    <HomePanel title="新鲜好物" sub-title="新鲜出炉 品质靠谱">
      <!-- "查看全部" 隐藏: 无新鲜好物列表页, router 中无对应路由 -->
      <template #right></template>
      <div style="position: relative; height: 406px" ref="target">
        <Transition name="fade">
          <!-- 面板内容 -->
          <ul v-if="goods.length" ref="pannel" class="goods-list">
            <li v-for="item in goods" :key="item.id">
              <RouterLink :to="`/product/${item.id}`">
                <img v-img-error :src="item.picture" alt="" />
                <p class="name ellipsis">{{ item.name }}</p>
                <p class="price">&yen;{{ item.price }}</p>
              </RouterLink>
            </li>
          </ul>
          <HomeSkeleton v-else bg="#f0f9f4" />
        </Transition>
      </div>
    </HomePanel>
  </div>
</template>
<script>
import HomePanel from "./home-panel";
import HomeSkeleton from "./home-skeleton";
import { findNew } from "@/api/home";
import { useLazyData } from "@/hooks";
export default {
  name: "HomeNew",
  components: { HomePanel, HomeSkeleton },
  setup() {
    const { target, result } = useLazyData(findNew);
    return { goods: result, target };
  },
};
</script>
<style scoped lang="less">
.xtx-more-link {
  display: inline-block;
  span {
    font-size: 16px;
    vertical-align: middle;
    margin-right: 4px;
    color: #999;
  }
  i {
    font-size: 14px;
    vertical-align: middle;
    position: relative;
    top: 2px;
    color: #ccc;
  }
  &:hover {
    span,
    i {
      color: @xtxColor;
    }
  }
}
.goods-list {
  display: flex;
  justify-content: space-between;
  height: 406px;
  li {
    width: 306px;
    height: 406px;
    background: #f0f9f4;
    .hoverShadow();
    img {
      width: 306px;
      height: 306px;
    }
    p {
      font-size: 22px;
      padding: 12px 30px 0 30px;
      text-align: center;
    }
    .price {
      color: @priceColor;
    }
  }
}
</style>
