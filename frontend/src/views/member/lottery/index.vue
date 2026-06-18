<template>
  <MemberFeatureShell eyebrow="Lucky Draw" title="幸运抽奖" summary="使用积分参与抽奖，可获得积分奖励或优惠券奖励。">
    <template #actions>
      <XtxButton type="primary" data-test="lottery-draw-btn" @click="drawOnce" :disabled="!info.chances || info.chances < 1">立即抽奖</XtxButton>
    </template>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">当前积分</div>
        <div class="value">{{ info.pointBalance || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">每次消耗</div>
        <div class="value">30 积分</div>
      </div>
      <div class="stat-card">
        <div class="label">可抽次数</div>
        <div class="value">{{ info.chances || 0 }}</div>
      </div>
    </div>
    <div class="formula-hint">可抽次数 = {{ info.pointBalance || 0 }} ÷ 30 = {{ info.chances || 0 }} 次</div>

    <div class="panel-grid">
      <section class="panel">
        <h3>奖池</h3>
        <div class="prize-grid">
          <div v-for="item in prizes" :key="item.name" class="prize-card">
            <strong>{{ item.name }}</strong>
            <span class="prize-probability">{{ item.probabilityPercent || (item.probability + '%') }}</span>
            <span>{{ prizeTypeMap[item.type] || item.type }}</span>
          </div>
        </div>
      </section>
      <section class="panel">
        <h3>抽奖记录</h3>
        <div v-if="history.length === 0" class="empty-tip">还没有抽奖记录。</div>
        <article v-for="item in history" :key="item.id" class="lottery-row">
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.createdAt }}</p>
          </div>
          <span>{{ prizeTypeMap[item.type] || item.type }}</span>
        </article>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { drawLottery, findLotteryInfo } from "@/api/member";

const prizeTypeMap = {
  points: "积分奖励",
  coupon: "优惠券奖励",
  none: "未中奖",
};

export default {
  name: "MemberLotteryPage",
  components: { MemberFeatureShell },
  setup() {
    const info = ref({});
    const prizes = computed(() => info.value.prizes || []);
    const history = computed(() => info.value.history || []);

    const load = async () => {
      const data = await findLotteryInfo();
      info.value = data.result || {};
    };

    const drawOnce = async () => {
      try {
        const data = await drawLottery();
        Message({ type: "success", text: `抽奖完成：${data.result.prize.name}` });
        await load();
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "抽奖失败" });
      }
    };

    onMounted(load);
    return { info, prizes, history, drawOnce, prizeTypeMap };
  },
};
</script>

<style scoped lang="less">
.prize-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.prize-card {
  padding: 18px;
  border-radius: 14px;
  background: #f7f8fa;
}

.prize-probability { color: #999; font-size: 12px; display: block; margin: 4px 0; }
.formula-hint { color: #999; font-size: 12px; text-align: center; margin-top: 8px; margin-bottom: 16px; }
.prize-card strong,
.lottery-row strong {
  display: block;
  margin-bottom: 6px;
}

.lottery-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #eef0f3;
}

.lottery-row:last-child {
  border-bottom: none;
}
</style>
