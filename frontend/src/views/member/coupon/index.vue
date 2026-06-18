<template>
  <MemberFeatureShell eyebrow="Coupon" title="优惠券" summary="支持查看当前可用券、已使用券，并通过兑换码领取新的优惠券。">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">可用券</div>
        <div class="value">{{ summary.available || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已使用</div>
        <div class="value">{{ summary.used || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已过期</div>
        <div class="value">{{ summary.expired || 0 }}</div>
      </div>
    </div>

    <div class="panel-grid">
      <section class="panel">
        <h3>兑换优惠券</h3>
        <div class="exchange-box">
          <input v-model.trim="code" data-test="coupon-code-input" class="code-input" placeholder="输入 XTX 开头兑换码" />
          <XtxButton type="primary" data-test="coupon-exchange-btn" @click="exchange">立即兑换</XtxButton>
        </div>
      </section>

      <section class="panel">
        <h3>优惠券列表</h3>
        <div v-if="items.length === 0" class="empty-tip">当前还没有可用优惠券。</div>
        <article v-for="item in items" :key="item.id" class="coupon-item">
          <div>
            <h4>{{ item.name }}</h4>
            <p>{{ item.description }}</p>
            <small>来源：{{ item.source }} · 有效期至 {{ item.expiresAt }}</small>
          </div>
          <div class="amount">
            <strong>¥{{ item.amount }}</strong>
            <span>{{ statusMap[item.status] || item.status }}</span>
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
import { exchangeMemberCoupon, findMemberCoupons } from "@/api/member";

const statusMap = {
  available: "可使用",
  used: "已使用",
  expired: "已过期",
};

export default {
  name: "MemberCouponPage",
  components: { MemberFeatureShell },
  setup() {
    const code = ref("");
    const items = ref([]);
    const summary = ref({});

    const load = async () => {
      const data = await findMemberCoupons();
      items.value = data.result.items || [];
      summary.value = data.result.summary || {};
    };

    const exchange = async () => {
      if (!code.value) {
        Message({ type: "warn", text: "请输入兑换码" });
        return;
      }
      try {
        await exchangeMemberCoupon(code.value);
        code.value = "";
        Message({ type: "success", text: "优惠券已发放到账户" });
        await load();
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "兑换失败" });
      }
    };

    onMounted(load);
    return { code, items, summary, exchange, statusMap };
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

.coupon-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eef0f3;
}

.coupon-item:last-child {
  border-bottom: none;
}

h4 {
  margin-bottom: 6px;
  font-size: 17px;
}

p,
small {
  color: #67707d;
}

.amount {
  min-width: 96px;
  text-align: right;
}

.amount strong {
  display: block;
  font-size: 28px;
  color: #d9485c;
}
</style>
