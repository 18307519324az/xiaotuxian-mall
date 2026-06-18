<template>
  <MemberFeatureShell eyebrow="Points" title="我的积分" summary="查看当前积分余额、即将过期积分以及最近的积分变动记录。">
    <div v-if="loading" class="loading-placeholder">
      <div class="loading-stats">
        <XtxSkeleton width="100%" height="80px" animated />
      </div>
      <div class="loading-list">
        <XtxSkeleton width="100%" height="120px" animated />
      </div>
    </div>
    <template v-else>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">当前积分</div>
        <div class="value">{{ info.balance || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">会员等级</div>
        <div class="value">{{ info.level || "-" }}</div>
      </div>
      <div class="stat-card">
        <div class="label">即将过期</div>
        <div class="value warn">{{ info.expiringSoon || 0 }}</div>
      </div>
    </div>

    <!-- 获取积分方式 -->
    <section class="list-card">
      <h3>获取积分方式</h3>
      <div class="earn-rules">
        <div class="earn-rule">
          <span class="earn-icon">📅</span>
          <div>
            <strong>每日签到</strong>
            <p>每日签到可获得 <b>+20</b> 积分，有效期 30 天</p>
            <button class="xtx-button" :disabled="signinDisabled" @click="handleSignin">
              {{ signinDisabled ? "今日已签到" : "签到领积分" }}
            </button>
            <span v-if="signinMessage" class="signin-msg">{{ signinMessage }}</span>
          </div>
        </div>
        <div class="earn-rule">
          <span class="earn-icon">👥</span>
          <div>
            <strong>邀请好友注册</strong>
            <p>邀请好友注册可获得 <b>+120</b> 积分/人，有效期 90 天</p>
            <RouterLink to="/member/invite" class="xtx-button-small">去邀请</RouterLink>
          </div>
        </div>
        <div class="earn-rule">
          <span class="earn-icon">🎰</span>
          <div>
            <strong>幸运抽奖</strong>
            <p>消耗 30 积分参与抽奖，有机会获得 <b>+10 / +20 / +50</b> 积分或优惠券</p>
            <RouterLink to="/member/lottery" class="xtx-button-small">去抽奖</RouterLink>
          </div>
        </div>
      </div>
      <div class="level-rules">
        <h4>会员等级规则</h4>
        <p>当前积分 0-499 = 普通会员 | 500-999 = 白银会员 | 1000-2999 = 黄金会员 | 3000-9999 = 白金会员 | 10000+ = 钻石会员</p>
        <p>积分消耗时优先扣除最早过期积分（FIFO）</p>
        <p class="expiry-note">即将过期积分 = 未来 30 天内到期的可用正向积分余额。积分过期后自动失效，不再计入可用积分。</p>
        <div class="reset-area">
          <button class="xtx-button-reset" @click="handleReset">{{ resetMessage || "重置积分演示数据" }}</button>
          <span class="reset-note">仅限本地演示，清空当前账号积分记录</span>
        </div>
      </div>
    </section>

    <!-- 积分明细 -->
    <section class="list-card">
      <h3>积分明细</h3>
      <div v-if="records.length === 0" class="empty-tip">当前没有积分变动记录。</div>
      <article v-for="item in records" :key="item.id" class="point-row">
        <div class="point-info">
          <strong>{{ item.title }}</strong>
          <p>获得时间：{{ formatTime(item.createdAt) }}</p>
          <p v-if="item.type === 'income' && item.expireAt" class="expire-info">
            过期时间：{{ formatTime(item.expireAt) }}
            <span v-if="item.remaining !== undefined"> · 剩余：{{ item.remaining }}</span>
          </p>
          <p v-if="item.type === 'expense'">消耗时间：{{ formatTime(item.createdAt) }}</p>
        </div>
        <span :class="item.type === 'income' ? 'income' : 'expense'">
          {{ item.type === "income" ? "+" : "" }}{{ item.delta }}
        </span>
      </article>
    </section>
    </template>
  </MemberFeatureShell>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import { findMemberPoints } from "@/api/member";
import request from "@/utils/request";

export default {
  name: "MemberPointsPage",
  components: { MemberFeatureShell },
  setup() {
    const formatTime = (value) => {
      if (!value) return '';
      return String(value).replace('T', ' ').replace(/\.\d+$/, '').slice(0, 19);
    };

    const info = ref({});
    const records = computed(() => info.value.records || []);
    const loading = ref(true);
    const signinDisabled = ref(false);
    const signinMessage = ref("");
    const resetMessage = ref("");

    const loadPoints = async () => {
      loading.value = true;
      const data = await findMemberPoints();
      info.value = data.result || {};
      loading.value = false;
      // 从后端 signedToday 初始化签到按钮状态
      signinDisabled.value = !!data.result?.signedToday;
    };

    const handleSignin = async () => {
      if (signinDisabled.value) return;
      try {
        const res = await request("/member/signin", "post");
        signinMessage.value = res.result?.message || "签到成功";
        signinDisabled.value = true;
        // 重新加载积分信息
        await loadPoints();
        // 3秒后清除提示
        setTimeout(() => { signinMessage.value = ""; }, 3000);
      } catch (e) {
        const msg = e.response?.data?.msg || e.message || "";
        if (msg.includes("今日已签到")) {
          signinDisabled.value = true;
          signinMessage.value = "今日已签到";
        } else {
          signinMessage.value = "签到失败，请重试";
        }
        setTimeout(() => { signinMessage.value = ""; }, 3000);
      }
    };

    const handleReset = async () => {
      if (resetMessage.value === "确认重置？") {
        try {
          await request("/member/points/reset", "post");
          resetMessage.value = "已重置";
          await loadPoints();
          signinDisabled.value = false;
        } catch (e) {
          resetMessage.value = "重置失败";
        }
        setTimeout(() => { resetMessage.value = ""; }, 3000);
      } else {
        resetMessage.value = "确认重置？";
        setTimeout(() => { resetMessage.value = ""; }, 5000);
      }
    };

    onMounted(async () => {
      await loadPoints();
    });

    return { info, records, signinDisabled, signinMessage, resetMessage, handleSignin, handleReset, formatTime };
  },
};
</script>

<style scoped lang="less">
.loading-placeholder {
  padding: 20px 0;
}
.loading-stats {
  margin-bottom: 20px;
}
.loading-list {
  margin-top: 20px;
}
.point-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #eef0f3;
}

.point-row:last-child {
  border-bottom: none;
}

.point-info {
  flex: 1;
  padding-right: 16px;
}

strong {
  display: block;
  margin-bottom: 4px;
}

p {
  color: #7c8592;
  margin: 2px 0;
  font-size: 13px;
}

.expire-info {
  color: #e6a23c;
}

.consume-info {
  color: #909399;
  font-style: italic;
}

.income,
.expense {
  font-size: 22px;
  font-weight: 600;
  white-space: nowrap;
}

.income {
  color: #2e9d58;
}

.expense {
  color: #d9485c;
}

.warn {
  color: #e6a23c;
}

.stats-grid {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-card .label {
  font-size: 14px;
  color: #7c8592;
  margin-bottom: 8px;
}

.stat-card .value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
}

.earn-rules {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}

.earn-rule {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.earn-icon {
  font-size: 28px;
  line-height: 1;
  margin-top: 2px;
}

.earn-rule p {
  margin: 4px 0;
}

.xtx-button {
  display: inline-block;
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  background: #27ba9b;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  margin-top: 6px;
}

.xtx-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.xtx-button-small {
  display: inline-block;
  padding: 4px 12px;
  border: 1px solid #27ba9b;
  border-radius: 4px;
  color: #27ba9b;
  text-decoration: none;
  font-size: 13px;
  margin-top: 6px;
}

.signin-msg {
  margin-left: 10px;
  color: #27ba9b;
  font-size: 13px;
}

.level-rules {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #eef0f3;
}

.level-rules h4 {
  margin: 0 0 6px;
  font-size: 14px;
}

.level-rules p {
  font-size: 13px;
  color: #7c8592;
  margin: 4px 0;
}

.expiry-note {
  color: #e6a23c !important;
}

.reset-area {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #eef0f3;
  display: flex;
  align-items: center;
  gap: 10px;
}

.xtx-button-reset {
  display: inline-block;
  padding: 5px 14px;
  border: 1px solid #e6a23c;
  border-radius: 4px;
  background: #fff;
  color: #e6a23c;
  cursor: pointer;
  font-size: 13px;
}

.xtx-button-reset:hover {
  background: #e6a23c;
  color: #fff;
}

.reset-note {
  font-size: 12px;
  color: #909399;
}
</style>
