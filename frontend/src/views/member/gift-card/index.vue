<template>
  <MemberFeatureShell eyebrow="Gift Card" title="礼品卡" summary="支持绑定新的礼品卡，并查看当前卡片余额与生效状态。">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">已绑定卡数</div>
        <div class="value">{{ summary.count || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">可用余额</div>
        <div class="value">¥{{ summary.balance || 0 }}</div>
      </div>
    </div>

    <div class="panel-grid">
      <section class="panel">
        <h3>绑定礼品卡</h3>
        <div class="exchange-box">
          <input v-model.trim="code" data-test="gift-card-code-input" class="code-input" placeholder="输入 GIFT 开头卡号" />
          <XtxButton type="primary" data-test="gift-card-bind-btn" @click="bind">立即绑定</XtxButton>
        </div>
      </section>

      <section class="panel">
        <h3>我的礼品卡</h3>
        <div v-if="cards.length === 0" class="empty-tip">还没有绑定任何礼品卡。</div>
        <article v-for="item in cards" :key="item.id" class="card-item">
          <div>
            <h4>{{ item.name }}</h4>
            <p>卡号：{{ item.code }}</p>
            <small>绑定时间：{{ item.boundAt }}</small>
          </div>
          <div class="card-meta">
            <strong>¥{{ item.balance }}</strong>
            <span>{{ item.status === "active" ? "可使用" : item.status }}</span>
          </div>
        </article>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { onMounted, ref } from "vue";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { bindGiftCard, findGiftCards } from "@/api/member";

export default {
  name: "MemberGiftCardPage",
  components: { MemberFeatureShell },
  setup() {
    const code = ref("");
    const cards = ref([]);
    const summary = ref({});

    const load = async () => {
      const data = await findGiftCards();
      cards.value = data.result.cards || [];
      summary.value = data.result.summary || {};
    };

    const bind = async () => {
      if (!code.value) {
        Message({ type: "warn", text: "请输入礼品卡卡号" });
        return;
      }
      try {
        await bindGiftCard(code.value);
        code.value = "";
        Message({ type: "success", text: "礼品卡绑定成功" });
        await load();
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "绑定失败" });
      }
    };

    onMounted(load);
    return { code, cards, summary, bind };
  },
};
</script>

<style scoped lang="less">
.exchange-box {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
  height: 42px;
  padding: 0 14px;
  border: 1px solid #d8dee6;
  border-radius: 12px;
}

.card-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eef0f3;
}

.card-item:last-child {
  border-bottom: none;
}

.card-meta {
  text-align: right;
}

.card-meta strong {
  display: block;
  font-size: 26px;
  color: #203a43;
}
</style>
