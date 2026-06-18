<template>
  <div class="page-brand">
    <div class="container">
      <div v-if="detailId" class="detail-page">
        <div class="crumb">
          <span class="link" @click="$router.push('/brand')">品牌馆</span>
          <span class="sep">/</span>
          <span>{{ detail.name || "品牌详情" }}</span>
        </div>
        <div v-if="loading" class="loading-detail">
          <XtxSkeleton animated bg="#e4e4e4" width="100%" height="320px" />
        </div>
        <template v-else-if="detail.id">
          <section class="hero">
            <div class="hero-copy">
              <img v-if="detail.logo" :src="detail.logo" :alt="detail.name" class="logo" />
              <p class="eyebrow">{{ detail.place || "品牌精选" }}</p>
              <h1>{{ detail.name }}</h1>
              <p class="desc">{{ detail.story || detail.desc }}</p>
              <div class="tags">
                <span v-for="tag in detail.serviceTags || []" :key="tag">{{ tag }}</span>
              </div>
              <div class="actions">
                <XtxButton type="primary" @click="toggleFollow">
                  {{ detail.followed ? "取消关注" : "关注品牌" }}
                </XtxButton>
                <XtxButton plain @click="$router.push('/member/brand-follow')">查看已关注品牌</XtxButton>
              </div>
            </div>
            <img :src="detail.picture || detail.logo" :alt="detail.name" class="hero-image" />
          </section>

          <section class="goods-section">
            <div class="section-head">
              <h3>品牌精选商品</h3>
            </div>
            <ul v-if="detail.goods?.length" class="goods-list">
              <li v-for="item in detail.goods" :key="item.id">
                <RouterLink :to="`/product/${item.id}`">
                  <img :src="item.picture" :alt="item.name" />
                  <p class="name ellipsis">{{ item.name }}</p>
                  <p class="desc ellipsis">{{ item.desc }}</p>
                  <p class="price">&yen;{{ item.price }}</p>
                </RouterLink>
              </li>
            </ul>
            <div v-else class="empty">
              <p>当前品牌暂无可展示商品</p>
            </div>
          </section>
        </template>
        <div v-else class="empty">
          <p>未找到对应品牌</p>
        </div>
      </div>

      <div v-else class="list-page">
        <div class="head">
          <h3>热门品牌</h3>
        </div>
        <div v-if="loading" class="loading">
          <XtxSkeleton
            class="item"
            v-for="i in 10"
            :key="i"
            animated
            bg="#e4e4e4"
            width="240px"
            height="305px"
          />
        </div>
        <ul v-else-if="brands.length" class="brand-list">
          <li v-for="item in brands" :key="item.id">
            <RouterLink :to="`/brand/${item.id}`">
              <img :src="item.picture" :alt="item.name" />
              <div class="info">
                <p class="name">{{ item.name }}</p>
                <p class="name-en">{{ item.place }}</p>
                <p class="desc ellipsis-2">{{ item.desc }}</p>
              </div>
            </RouterLink>
          </li>
        </ul>
        <div v-else class="empty">
          <p>暂无品牌数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import store from "@/store";
import Message from "@/components/library/Message";
import { findBrand, findBrandDetail } from "@/api/home";
import { addBrandFollow, deleteBrandFollow } from "@/api/member";

export default {
  name: "BrandPage",
  setup() {
    const route = useRoute();
    const router = useRouter();
    const brands = ref([]);
    const detail = ref({});
    const loading = ref(false);
    const detailId = computed(() => route.params.id || "");

    const loadList = async () => {
      loading.value = true;
      try {
        const data = await findBrand(100);
        brands.value = data.result || [];
      } finally {
        loading.value = false;
      }
    };

    const loadDetail = async (id) => {
      loading.value = true;
      try {
        const data = await findBrandDetail(id);
        detail.value = data.result || {};
      } catch (err) {
        detail.value = {};
        Message({ type: "error", text: err.response?.data?.msg || "加载品牌详情失败" });
      } finally {
        loading.value = false;
      }
    };

    const reload = async () => {
      if (detailId.value) {
        await loadDetail(detailId.value);
      } else {
        detail.value = {};
        await loadList();
      }
    };

    const toggleFollow = async () => {
      const token = store.state.user.profile.token;
      if (!token) {
        Message({ type: "warn", text: "请先登录后再关注品牌" });
        router.push(`/login?redirectUrl=${encodeURIComponent(route.fullPath)}`);
        return;
      }
      try {
        if (detail.value.followed) {
          await deleteBrandFollow(detail.value.id);
          detail.value.followed = false;
          Message({ type: "success", text: "已取消关注品牌" });
        } else {
          await addBrandFollow(detail.value.id);
          detail.value.followed = true;
          Message({ type: "success", text: "关注品牌成功" });
        }
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "品牌关注操作失败" });
      }
    };

    watch(() => route.fullPath, reload, { immediate: true });

    return {
      brands,
      detail,
      detailId,
      loading,
      toggleFollow,
    };
  },
};
</script>

<style scoped lang="less">
.page-brand {
  background: #f5f5f5;
  min-height: 600px;
  padding-bottom: 40px;
}

.head {
  padding: 32px 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    font-size: 28px;
    font-weight: 500;
    color: #333;
    position: relative;
    padding-left: 16px;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 6px;
      bottom: 6px;
      width: 4px;
      background: @xtxColor;
      border-radius: 2px;
    }
  }
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h3 {
    font-size: 20px;
    font-weight: 500;
    color: #333;
    position: relative;
    padding-left: 14px;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 5px;
      bottom: 5px;
      width: 4px;
      background: @xtxColor;
      border-radius: 2px;
    }
  }
}

.crumb {
  padding: 20px 0;
  color: #666;
  font-size: 14px;

  .link {
    cursor: pointer;
    color: @xtxColor;

    &:hover {
      opacity: 0.8;
    }
  }

  .sep {
    margin: 0 8px;
    color: #ccc;
  }
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 24px;
  align-items: stretch;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.eyebrow {
  color: @xtxColor;
  font-size: 14px;
  letter-spacing: 1px;
}

h1 {
  margin: 12px 0 16px;
  font-size: 30px;
  color: #333;
  font-weight: 600;
}

.desc {
  color: #666;
  line-height: 1.8;
  font-size: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;

  span {
    padding: 6px 12px;
    background: #f2fbf9;
    color: @xtxColor;
    border-radius: 999px;
    font-size: 12px;
  }
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.hero-image {
  width: 100%;
  height: 320px;
  object-fit: cover;
  border-radius: 8px;
}

.goods-section {
  margin-top: 24px;
  padding: 0 0 8px;
}

.loading,
.goods-list,
.brand-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.goods-list li,
.brand-list li {
  width: 240px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  .hoverShadow();
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.goods-list a,
.brand-list a {
  display: block;
}

.goods-list img,
.brand-list img {
  width: 240px;
  height: 240px;
  object-fit: cover;
}

.info,
.goods-list a {
  padding-bottom: 12px;
}

.info {
  padding: 12px;
}

.name {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.name-en {
  font-size: 13px;
  color: @xtxColor;
  margin-top: 4px;
}

.brand-list .desc {
  margin-top: 8px;
  font-size: 13px;
  color: #999;
  line-height: 1.5;
}

.goods-list .name,
.goods-list .desc,
.goods-list .price {
  padding: 0 12px;
}

.goods-list .name {
  margin-top: 12px;
  font-size: 14px;
}

.goods-list .desc {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}

.goods-list .price {
  margin-top: 8px;
  color: @priceColor;
  font-size: 18px;
  font-weight: 600;
}

.loading .item {
  width: 240px;
  height: 305px;
}

.empty {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 16px;
}

@media (max-width: 1240px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
