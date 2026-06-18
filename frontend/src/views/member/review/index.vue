<template>
  <MemberFeatureShell title="评价晒单" summary="查看待评价商品和历史评价，支持公开评价与匿名评价两种身份。">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">待评价</div>
        <div class="value">{{ stats.pending || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已完成</div>
        <div class="value">{{ stats.completed || 0 }}</div>
      </div>
    </div>

    <div class="panel-grid">
      <section class="panel">
        <h3>待提交评价</h3>
        <div v-if="pendingItems.length === 0" class="empty-tip">当前没有待评价商品。</div>
        <article v-for="item in pendingItems" :key="item.id" class="review-item">
          <div class="review-head">
            <img :src="item.image" :alt="item.name" />
            <div class="review-meta">
              <h4>{{ item.name }}</h4>
              <p>{{ item.attrsText }}</p>
              <small>订单号：{{ item.orderId }}</small>
            </div>
          </div>
          <div class="editor">
            <label>
              评分
              <select v-model.number="drafts[item.id].score">
                <option :value="5">5 分</option>
                <option :value="4">4 分</option>
                <option :value="3">3 分</option>
                <option :value="2">2 分</option>
                <option :value="1">1 分</option>
              </select>
            </label>
            <div class="identity-group" data-test="review-identity-group">
              <span class="identity-label">评价身份：</span>
              <label class="identity-option">
                <input v-model="drafts[item.id].anonymous" :value="false" type="radio" :name="`identity-${item.id}`" />
                公开评价
              </label>
              <label class="identity-option">
                <input v-model="drafts[item.id].anonymous" :value="true" type="radio" :name="`identity-${item.id}`" />
                匿名评价
              </label>
            </div>
            <textarea v-model.trim="drafts[item.id].content" placeholder="写下对商品的真实体验" />
            <XtxButton type="primary" data-test="review-submit-btn" @click="submit(item)">提交评价</XtxButton>
          </div>
        </article>
      </section>

      <section class="panel">
        <h3>历史评价</h3>
        <div v-if="completedItems.length === 0" class="empty-tip">暂时还没有历史评价。</div>
        <article v-for="item in completedItems" :key="item.id" class="history-item">
          <div class="history-head">
            <img :src="reviewImage(item)" :alt="reviewName(item)" class="history-pic" @error="onHistoryImgError" />
            <div class="history-meta">
              <strong>{{ reviewName(item) }}</strong>
              <p class="attrs">{{ item.attrsText }}</p>
              <small>订单号：{{ item.orderId }}</small>
            </div>
          </div>
          <div class="history-body">
            <p class="content">{{ item.content || "已提交评价" }}</p>
            <small class="footnote">{{ displayIdentity(item) }} · {{ item.createdAt || "已完成" }}</small>
          </div>
          <span class="score">{{ item.score }} 分</span>
        </article>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useStore } from "vuex";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { findMemberReviews, submitMemberReview } from "@/api/member";

export default {
  name: "MemberReviewPage",
  components: { MemberFeatureShell },
  setup() {
    const route = useRoute();
    const store = useStore();
    const items = ref([]);
    const stats = ref({});
    const drafts = reactive({});

    const currentNickname = computed(() => store.state.user.profile.nickname || "小兔鲜会员");
    const pendingItems = computed(() => items.value.filter((item) => item.status === "pending"));
    const completedItems = computed(() => items.value.filter((item) => item.status === "completed"));

    const ensureDraft = (item) => {
      if (!drafts[item.id]) {
        drafts[item.id] = {
          score: 5,
          content: "",
          anonymous: false,
        };
      }
    };

    const displayIdentity = (item) => {
      if (item.anonymous) return "匿名用户";
      return item.reviewerName || currentNickname.value;
    };

    const load = async () => {
      const params = route.query.orderId ? { orderId: route.query.orderId } : {};
      const data = await findMemberReviews(params);
      items.value = data.result.items || [];
      stats.value = data.result.stats || {};
      items.value.forEach(ensureDraft);
    };

    const REVIEW_FALLBACK_IMG = 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/7f6a7b20-7902-4b43-b9c5-f33151ef1334.jpg';
    const onHistoryImgError = (e) => {
      e.target.src = REVIEW_FALLBACK_IMG;
    };

    // 统一兜底：商品名
    const reviewName = (item) => {
      return item.name || item.title || item.goodsName || item.productName || "商品";
    };

    // 统一兜底：商品图
    const reviewImage = (item) => {
      return item.picture || item.image || item.pic || item.goodsPicture || REVIEW_FALLBACK_IMG;
    };

    const submit = async (item) => {
      const draft = drafts[item.id];
      if (!draft.content) {
        Message({ type: "warn", text: "请填写评价内容" });
        return;
      }
      await submitMemberReview({
        id: item.id,
        score: draft.score,
        content: draft.content,
        anonymous: draft.anonymous === true || draft.anonymous === "true",
      });
      Message({ type: "success", text: "评价提交成功" });
      await load();
    };

    watch(() => route.query.orderId, load);
    onMounted(load);
    return { stats, drafts, pendingItems, completedItems, submit, displayIdentity, onHistoryImgError, reviewName, reviewImage };
  },
};
</script>

<style scoped lang="less">
.review-item,
.history-item {
  padding: 18px 0;
  border-bottom: 1px solid #f5f5f5;
}

.review-item:last-child,
.history-item:last-child {
  border-bottom: none;
}

.review-head {
  display: flex;
  gap: 14px;
}

.review-head img {
  width: 88px;
  height: 88px;
  border: 1px solid #f5f5f5;
  object-fit: cover;
}

.review-meta h4 {
  margin-bottom: 8px;
  font-size: 16px;
  color: #333;
}

.review-meta p,
.review-meta small,
.history-item p,
.history-item small {
  color: #999;
  line-height: 1.7;
}

.editor {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.editor select,
.editor textarea {
  border: 1px solid #e4e4e4;
  padding: 10px 12px;
}

.editor textarea {
  min-height: 110px;
  resize: vertical;
}

.identity-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  color: #666;
}

.identity-label {
  color: #666;
}

.identity-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.history-item {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.history-head {
  display: flex;
  gap: 14px;
  flex: 1 1 100%;
}

.history-pic {
  width: 88px;
  height: 88px;
  border: 1px solid #f5f5f5;
  object-fit: cover;
  flex-shrink: 0;
}

.history-meta {
  flex: 1;
}

.history-meta strong {
  font-size: 16px;
  color: #333;
}

.history-meta .attrs {
  color: #999;
  margin-top: 6px;
  font-size: 13px;
}

.history-meta small {
  color: #999;
  font-size: 12px;
}

.history-body {
  flex: 1 1 100%;
}

.history-body .content {
  color: #666;
  line-height: 1.7;
}

.history-body .footnote {
  color: #999;
  font-size: 12px;
}

.score {
  font-size: 20px;
  font-weight: 600;
  color: @xtxColor;
}
</style>
