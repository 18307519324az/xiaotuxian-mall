<template>
  <MemberFeatureShell eyebrow="Inbox" title="消息通知" summary="系统提醒、活动动态和服务通知都会集中展示在这里。">
    <template #actions>
      <XtxButton type="primary" size="small" data-test="message-read-all" @click="readAll">全部标为已读</XtxButton>
    </template>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="label">总消息</div>
        <div class="value">{{ summary.total }}</div>
      </div>
      <div class="stat-card">
        <div class="label">未读消息</div>
        <div class="value">{{ summary.unread }}</div>
      </div>
    </div>

    <div class="list-card">
      <h3>最近消息</h3>
      <div v-if="items.length === 0" class="empty-tip">当前没有新的消息通知。</div>
      <article v-for="item in items" :key="item.id" class="message-item" :class="{ unread: !item.isRead }" data-test="message-item">
        <div class="meta">
          <span class="type">{{ item.type }}</span>
          <span class="time">{{ item.createdAt }}</span>
        </div>
        <h4>{{ item.title }}</h4>
        <p>{{ item.content }}</p>
        <button v-if="!item.isRead" type="button" class="text-btn" data-test="message-read-btn" @click="readOne(item.id)">标为已读</button>
      </article>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { findMemberMessages, readMemberMessage } from "@/api/member";

export default {
  name: "MemberMessagePage",
  components: { MemberFeatureShell },
  setup() {
    const items = ref([]);
    const summary = ref({ total: 0, unread: 0 });
    const unreadIds = computed(() => items.value.filter((item) => !item.isRead).map((item) => item.id));

    const load = async () => {
      const data = await findMemberMessages();
      items.value = data.result.items || [];
      summary.value = data.result.summary || { total: 0, unread: 0 };
    };

    const readOne = async (id) => {
      await readMemberMessage({ id });
      Message({ type: "success", text: "消息已标记为已读" });
      await load();
    };

    const readAll = async () => {
      if (unreadIds.value.length === 0) return;
      await readMemberMessage({ readAll: true });
      Message({ type: "success", text: "所有消息已读" });
      await load();
    };

    onMounted(load);
    return { items, summary, readOne, readAll };
  },
};
</script>

<style scoped lang="less">
.message-item {
  padding: 18px 0;
  border-bottom: 1px solid #f0f2f5;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item.unread h4::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  border-radius: 50%;
  background: #d9485c;
  vertical-align: middle;
}

.meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #88909d;
  font-size: 13px;
}

h4 {
  margin-bottom: 8px;
  font-size: 18px;
  color: #222;
}

p {
  color: #5f6773;
  line-height: 1.8;
}

.text-btn {
  margin-top: 12px;
  border: none;
  background: none;
  color: @xtxColor;
  cursor: pointer;
}
</style>
