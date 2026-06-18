<template>
  <MemberFeatureShell eyebrow="Invite" title="邀请有礼" summary="分享邀请码给好友，好友注册或下单后即可累计奖励积分。">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-placeholder">
      <div class="loading-stats">
        <XtxSkeleton width="100%" height="80px" animated />
      </div>
    </div>
    <template v-else>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">邀请码</div>
        <div class="value" data-test="invite-code">{{ info.inviteCode || "-" }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已邀请人数</div>
        <div class="value">{{ info.invitedCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">累计奖励积分</div>
        <div class="value">{{ info.rewardPoints || 0 }}</div>
      </div>
    </div>

    <div class="panel-grid">
      <section class="panel">
        <h3>分享入口</h3>
        <p class="share-link">{{ shareLink }}</p>
        <XtxButton type="primary" data-test="invite-copy-btn" @click="copyCode">复制邀请码</XtxButton>
        <XtxButton type="plain" data-test="invite-copy-share-btn" @click="copyShareLink" style="margin-left: 10px;">复制分享链接</XtxButton>
      </section>
      <section class="panel">
        <h3>邀请记录</h3>
        <div v-if="records.length === 0" class="empty-tip">暂时没有邀请记录。</div>
        <article v-for="item in records" :key="item.id" class="invite-row">
          <div>
            <strong>{{ displayName(item) }}</strong>
            <p>{{ item.createdAt }}</p>
          </div>
          <div class="invite-meta">
            <span>{{ item.status }}</span>
            <strong>+{{ item.rewardPoints }}</strong>
          </div>
        </article>
      </section>
    </div>
    </template>
  </MemberFeatureShell>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { findInviteInfo } from "@/api/member";

export default {
  name: "MemberInvitePage",
  components: { MemberFeatureShell },
  setup() {
    const info = ref({});
    const records = computed(() => info.value.records || []);
    const loading = ref(true);

    // 前端生成真实分享链接（不再依赖后端返回 xtx.example.com 假域名）
    const shareLink = computed(() => {
      const code = info.value.inviteCode || '';
      if (!code) return '暂无邀请码';
      const origin = window.location.origin;
      return origin + '/#/login?mode=register&inviteCode=' + encodeURIComponent(code);
    });

    // 前端双重兜底：过滤技术 ID 模式，展示可读名称
    const techIdPattern = /^(user_|inv_|dup_|mock-token|\d{10,})/;
    const displayName = (item) => {
      if (!item) return '好友用户';
      const name = item.friendName || item.inviteeName || item.inviteeAccount || '';
      if (name && !techIdPattern.test(name)) return name;
      return '好友用户';
    };

    const copyCode = async () => {
      const code = info.value.inviteCode || "";
      if (!code) return;
      try {
        if (navigator.clipboard?.writeText) {
          await navigator.clipboard.writeText(code);
        } else {
          const input = document.createElement("input");
          input.value = code;
          document.body.appendChild(input);
          input.select();
          document.execCommand("copy");
          document.body.removeChild(input);
        }
        Message({ type: "success", text: "邀请码已复制" });
      } catch (err) {
        Message({ type: "error", text: "复制失败，请手动复制" });
      }
    };

    const copyShareLink = async () => {
      const link = shareLink.value;
      if (!link || link === '暂无邀请码') return;
      try {
        if (navigator.clipboard?.writeText) {
          await navigator.clipboard.writeText(link);
        } else {
          const input = document.createElement("input");
          input.value = link;
          document.body.appendChild(input);
          input.select();
          document.execCommand("copy");
          document.body.removeChild(input);
        }
        Message({ type: "success", text: "分享链接已复制" });
      } catch (err) {
        Message({ type: "error", text: "复制失败，请手动复制" });
      }
    };

    onMounted(async () => {
      loading.value = true;
      const data = await findInviteInfo();
      info.value = data.result || {};
      loading.value = false;
    });

    return { info, records, loading, shareLink, copyCode, copyShareLink, displayName };
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
.share-link {
  margin-bottom: 16px;
  color: #56606d;
  line-height: 1.8;
  word-break: break-all;
}

.invite-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eef0f3;
}

.invite-row:last-child {
  border-bottom: none;
}

.invite-meta {
  text-align: right;
}

.invite-meta strong {
  display: block;
  margin-top: 6px;
  color: #2e9d58;
}
</style>
