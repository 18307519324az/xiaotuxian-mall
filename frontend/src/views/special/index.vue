<template>
  <div class="page-special">
    <div class="container">
      <div v-if="detailId" class="detail-page">
        <div class="crumb">
          <span class="link" @click="$router.push('/special')">专题广场</span>
          <span class="sep">/</span>
          <span>{{ detail.title || "专题详情" }}</span>
        </div>

        <div v-if="loading" class="loading-detail">
          <XtxSkeleton animated bg="#e4e4e4" width="100%" height="320px" />
        </div>
        <template v-else-if="detail.id">
          <section class="hero">
            <div class="copy">
              <p class="eyebrow">{{ detail.theme || "专题精选" }}</p>
              <h1>{{ detail.title }}</h1>
              <p class="summary">{{ detail.summary }}</p>
              <div class="meta">
                <span>收藏 {{ detail.collectNum || 0 }}</span>
                <span>浏览 {{ detail.viewNum || 0 }}</span>
                <span>评论 {{ detail.replyNum || 0 }}</span>
              </div>
              <div class="actions">
                <XtxButton type="primary" @click="toggleCollect">
                  {{ detail.collected ? "取消收藏专题" : "收藏专题" }}
                </XtxButton>
                <XtxButton plain @click="$router.push('/member/topic-collect')">查看已收藏专题</XtxButton>
              </div>
            </div>
            <img :src="getTopicImage(detail)" :alt="detail.title" class="cover" @error="onImgError" />
          </section>

          <section v-if="detail.detailBlocks?.length" class="detail-blocks">
            <article v-for="(block, index) in detail.detailBlocks" :key="`${detail.id}-${index}`" class="block">
              {{ block }}
            </article>
          </section>

          <section class="goods-section">
            <div class="head">
              <h3>专题精选好物</h3>
            </div>
            <ul v-if="detail.goods?.length" class="goods-list">
              <li v-for="item in detail.goods" :key="item.id">
                <RouterLink :to="`/product/${item.id}`">
                  <img :src="item.picture || item.image" :alt="item.name" @error="onImgError" />
                  <p class="name ellipsis">{{ item.name }}</p>
                  <p class="desc ellipsis">{{ item.desc }}</p>
                  <p class="price">&yen;{{ item.price }}</p>
                </RouterLink>
              </li>
            </ul>
            <div v-else class="empty">
              <p>当前专题暂无商品</p>
            </div>
          </section>
        </template>
        <div v-else class="empty">
          <p>未找到对应专题</p>
        </div>
      </div>

      <div v-else class="list-page">
        <div class="head">
          <h3>最新专题</h3>
        </div>
        <div v-if="loading" class="loading">
          <XtxSkeleton
            class="item"
            v-for="i in 6"
            :key="i"
            animated
            bg="#e4e4e4"
            width="404px"
            height="380px"
          />
        </div>
        <ul v-else-if="specialList.length" class="special-list">
          <li v-for="item in specialList" :key="item.id" @click="$router.push(`/special/${item.id}`)">
            <div class="cover-wrap">
              <img :src="getTopicImage(item)" :alt="item.title" @error="onImgError" />
              <div class="meta-mask">
                <p class="title">
                  <span class="top ellipsis">{{ item.title }}</span>
                  <span class="sub ellipsis">{{ item.summary }}</span>
                </p>
                <span class="price">&yen;{{ item.lowestPrice }}起</span>
              </div>
            </div>
            <div class="foot">
              <span class="like"><i class="iconfont icon-hart1"></i>{{ item.collectNum }}</span>
              <span class="view"><i class="iconfont icon-see"></i>{{ item.viewNum }}</span>
              <span class="reply"><i class="iconfont icon-message"></i>{{ item.replyNum }}</span>
            </div>
          </li>
        </ul>
        <div v-else class="empty">
          <p>暂无专题数据</p>
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
import { findSpecial, findSpecialDetail } from "@/api/home";
import { addTopicCollect, deleteTopicCollect } from "@/api/member";

const FALLBACK_TOPIC_IMAGE = 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/7f6a7b20-7902-4b43-b9c5-f33151ef1334.jpg';

function isInvalidTopicImage(src) {
  if (!src) return true;
  const lower = src.toLowerCase();
  return lower.includes('none') ||
    lower.includes('placeholder') ||
    lower.includes('default') ||
    lower.includes('jnnww') ||
    lower.includes('jiangnan') ||
    lower.includes('brand');
}

function getTopicImage(topic) {
  // If backend says goods-first, use first valid goods picture directly
  // (cover/banner/picture old fields cannot override this)
  if (topic.coverSource === 'goods-first' && topic.goods) {
    for (const goods of topic.goods) {
      if (goods.picture && !isInvalidTopicImage(goods.picture)) return goods.picture;
    }
  }
  // Use resolvedCover from backend if available and valid
  if (topic.resolvedCover && !isInvalidTopicImage(topic.resolvedCover)) {
    return topic.resolvedCover;
  }
  // Fallback chain with invalid image detection
  if (topic.cover && !isInvalidTopicImage(topic.cover)) return topic.cover;
  if (topic.banner && !isInvalidTopicImage(topic.banner)) return topic.banner;
  if (topic.picture && !isInvalidTopicImage(topic.picture)) return topic.picture;
  // First valid goods picture
  if (topic.goods) {
    for (const goods of topic.goods) {
      if (goods.picture && !isInvalidTopicImage(goods.picture)) return goods.picture;
    }
  }
  return FALLBACK_TOPIC_IMAGE;
}

function onImgError(e) {
  e.target.src = FALLBACK_TOPIC_IMAGE;
}

export default {
  name: "SpecialPage",
  setup() {
    const route = useRoute();
    const router = useRouter();
    const specialList = ref([]);
    const detail = ref({});
    const loading = ref(false);
    const detailId = computed(() => route.params.id || "");

    const loadList = async () => {
      loading.value = true;
      try {
        const data = await findSpecial();
        specialList.value = data.result || [];
      } finally {
        loading.value = false;
      }
    };

    const loadDetail = async (id) => {
      loading.value = true;
      try {
        const data = await findSpecialDetail(id);
        detail.value = data.result || {};
      } catch (err) {
        detail.value = {};
        Message({ type: "error", text: err.response?.data?.msg || "加载专题详情失败" });
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

    const toggleCollect = async () => {
      const token = store.state.user.profile.token;
      if (!token) {
        Message({ type: "warn", text: "请先登录后再收藏专题" });
        router.push(`/login?redirectUrl=${encodeURIComponent(route.fullPath)}`);
        return;
      }
      try {
        if (detail.value.collected) {
          await deleteTopicCollect(detail.value.id);
          detail.value.collected = false;
          Message({ type: "success", text: "已取消收藏专题" });
        } else {
          await addTopicCollect(detail.value.id);
          detail.value.collected = true;
          Message({ type: "success", text: "收藏专题成功" });
        }
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "专题收藏操作失败" });
      }
    };

    watch(() => route.fullPath, reload, { immediate: true });

    return {
      detail,
      detailId,
      loading,
      specialList,
      toggleCollect,
      getTopicImage,
      onImgError,
    };
  },
};
</script>

<style scoped lang="less">
.page-special {
  background: #f5f5f5;
  min-height: 600px;
  padding-bottom: 40px;
}

.crumb {
  padding: 24px 0;
  color: #666;
  font-size: 14px;
}

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

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 24px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  color: @xtxColor;
  font-size: 14px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

h1 {
  margin: 12px 0 16px;
  font-size: 30px;
  color: #333;
  font-weight: 600;
}

.summary {
  color: #666;
  line-height: 1.8;
  font-size: 14px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 18px;
  color: #999;
  font-size: 13px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cover {
  width: 100%;
  height: 320px;
  object-fit: cover;
  border-radius: 8px;
}

.detail-blocks {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.block {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  color: #555;
  line-height: 1.8;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s, transform 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.loading,
.special-list,
.goods-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.special-list li {
  width: 404px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  .hoverShadow();
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.cover-wrap {
  position: relative;
  height: 288px;
  overflow: hidden;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.special-list li:hover .cover-wrap img {
  transform: scale(1.05);
}

.meta-mask {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent 50%);
}

.title {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 70%;
  padding-left: 16px;
  padding-bottom: 8px;
}

.top {
  display: block;
  color: #fff;
  font-size: 22px;
  font-weight: 600;
}

.sub {
  display: block;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-top: 4px;
}

.price {
  position: absolute;
  right: 16px;
  bottom: 25px;
  padding: 4px 10px;
  background: #fff;
  border-radius: 4px;
  color: @priceColor;
  font-size: 16px;
  font-weight: 600;
}

.foot {
  height: 64px;
  line-height: 64px;
  padding: 0 20px;
  font-size: 14px;
  color: #999;

  i {
    display: inline-block;
    width: 16px;
    height: 14px;
    margin-right: 5px;
    color: #bbb;
    font-size: 14px;
    vertical-align: -1px;
  }
}

.like,
.view {
  float: left;
  margin-right: 25px;
}

.reply {
  float: right;
}

.goods-section {
  margin-top: 24px;

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
}

.goods-list li {
  width: 240px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  .hoverShadow();
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.goods-list a {
  display: block;
  padding-bottom: 12px;
}

.goods-list img {
  width: 240px;
  height: 240px;
  object-fit: cover;
}

.goods-list .name,
.goods-list .desc,
.goods-list .price {
  padding: 0 12px;
}

.goods-list .name {
  margin-top: 12px;
  color: #333;
  font-weight: 700;
  font-size: 14px;
}

.goods-list .desc {
  margin-top: 6px;
  color: #999;
  font-size: 12px;
}

.goods-list .price {
  position: static;
  margin-top: 8px;
  color: @priceColor;
  font-size: 18px;
  font-weight: 600;
}

.empty {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 16px;
}

@media (max-width: 1240px) {
  .hero,
  .detail-blocks {
    grid-template-columns: 1fr;
  }
}
</style>
