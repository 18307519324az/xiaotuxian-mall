<template>
  <ul class="goods-sales">
    <li>
      <p>销量人气</p>
      <p>200+</p>
      <p class="display-only"><i class="iconfont icon-task-filling"></i>销量人气</p>
    </li>
    <li>
      <p>商品评价</p>
      <p>400+</p>
      <p class="action-link" @click="handleViewEvaluation"><i class="iconfont icon-comment-filling"></i>查看评价</p>
    </li>
    <li>
      <p>收藏人气</p>
      <p>600+</p>
      <p class="action-link" :class="{ collected: isCollected }" @click="handleCollect">
        <i class="iconfont" :class="isCollected ? 'icon-favorite' : 'icon-favorite-filling'"></i>{{ isCollected ? '已收藏' : '收藏商品' }}
      </p>
    </li>
    <li>
      <p>品牌信息</p>
      <p>{{ brandName }}</p>
      <p class="action-link" @click="handleBrandPage"><i class="iconfont icon-dynamic-filling"></i>品牌主页</p>
    </li>
  </ul>
</template>

<script>
import Message from "@/components/library/Message";
export default {
  name: "GoodsSales",
  inject: {
    // Use callback-based approach: Vue 3.3+ auto-unwraps refs in Options API inject,
    // so we cannot inject the ref directly — inject a setter function instead
    switchTab: { from: "switchTab", default: null },
    activeGoodsTab: { from: "activeGoodsTab", default: null },
  },
  props: {
    goods: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      isCollected: false,
    };
  },
  computed: {
    brandName() {
      if (this.goods && this.goods.brand && this.goods.brand.name) {
        return this.goods.brand.name;
      }
      return "传智自有品牌";
    },
  },
  methods: {
    handleViewEvaluation() {
      // Switch to comment tab via callback (Vue 3.3+ auto-unwraps refs in Options API)
      if (this.switchTab) {
        this.switchTab("comment");
      }
      // Scroll down to the goods tabs / evaluation area
      this.$nextTick(() => {
        const el = document.querySelector(".goods-tabs");
        if (el) {
          el.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });
    },
    handleCollect() {
      // Check if user is logged in
      const token = this.$store?.state?.user?.profile?.token;
      if (!token) {
        // Not logged in — prompt login
        Message({ text: "请先登录后再收藏" });
        // Optionally redirect to login page
        // this.$router.push("/login");
        return;
      }
      // Toggle collect state
      this.isCollected = !this.isCollected;
      if (this.isCollected) {
        Message({ type: "success", text: "收藏成功" });
      } else {
        Message({ text: "已取消收藏" });
      }
    },
    handleBrandPage() {
      const brandId = this.goods?.brand?.id;
      if (brandId && brandId !== "brand_default") {
        this.$router.push(`/brand/${brandId}`);
        return;
      }
      this.$router.push("/brand");
    },
  },
};
</script>

<style scoped lang="less">
.goods-sales {
  display: flex;
  width: 400px;
  align-items: center;
  text-align: center;
  height: 140px;
  li {
    flex: 1;
    position: relative;
    ~ li::after {
      position: absolute;
      top: 10px;
      left: 0;
      height: 60px;
      border-left: 1px solid #e4e4e4;
      content: "";
    }
    p {
      &:first-child {
        color: #999;
      }
      &:nth-child(2) {
        color: @priceColor;
        margin-top: 10px;
      }
      &:last-child {
        color: #666;
        margin-top: 10px;
        i {
          color: @xtxColor;
          font-size: 14px;
          margin-right: 2px;
        }
      }
      // Display-only items: no pointer, no hover effect
      &.display-only {
        cursor: default;
        &:hover {
          color: #666;
        }
      }
      // Action items: pointer + hover
      &.action-link {
        cursor: pointer;
        &:hover {
          color: @xtxColor;
        }
        // Collected state
        &.collected {
          color: @xtxColor;
        }
      }
    }
  }
}
</style>
