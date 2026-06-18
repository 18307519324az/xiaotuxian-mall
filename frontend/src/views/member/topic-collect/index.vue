<template>
  <MemberFeatureShell title="收藏的专题" summary="专题收藏列表已接入真实接口，并与专题详情页联动。">
    <div v-if="loading" class="loading-list">
      <XtxSkeleton v-for="i in 3" :key="i" animated bg="#e4e4e4" width="100%" height="160px" />
    </div>
    <div v-else class="topic-list">
      <article class="topic-card portal-card">
        <div class="cover placeholder">专题广场</div>
        <div class="content">
          <div>
            <strong>前往专题广场</strong>
            <p class="summary">进入专题广场查看完整专题列表和对应的专题详情。</p>
          </div>
          <div class="card-actions">
            <XtxButton plain @click="$router.push('/special')">前往专题广场</XtxButton>
          </div>
        </div>
      </article>
      <article v-for="item in items" :key="item.id" class="topic-card">
        <img :src="item.cover" :alt="item.title" class="cover" />
        <div class="content">
          <div>
            <strong>{{ item.title }}</strong>
            <p class="summary">{{ item.summary }}</p>
            <ul class="meta">
              <li>收藏 {{ item.collectNum || 0 }}</li>
              <li>浏览 {{ item.viewNum || 0 }}</li>
              <li>评论 {{ item.replyNum || 0 }}</li>
            </ul>
          </div>
          <div class="card-actions">
            <XtxButton type="primary" plain @click="viewTopic(item)">查看专题</XtxButton>
            <XtxButton type="gray" plain @click="removeTopic(item)">取消收藏</XtxButton>
          </div>
        </div>
      </article>
      <div v-if="items.length === 0" class="empty-tip">暂无收藏专题，去专题广场逛逛。</div>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { ref } from "vue";
import { useRouter } from "vue-router";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { deleteTopicCollect, findTopicCollect } from "@/api/member";

export default {
  name: "MemberTopicCollectPage",
  components: { MemberFeatureShell },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const items = ref([]);

    const loadData = async () => {
      loading.value = true;
      try {
        const data = await findTopicCollect({ page: 1, pageSize: 20 });
        items.value = data.result?.items || [];
      } catch (err) {
        items.value = [];
        Message({ type: "error", text: err.response?.data?.msg || "加载专题收藏失败" });
      } finally {
        loading.value = false;
      }
    };

    const viewTopic = (item) => {
      router.push(`/special/${item.id}`);
    };

    const removeTopic = async (item) => {
      try {
        await deleteTopicCollect(item.id);
        Message({ type: "success", text: "已取消收藏专题" });
        await loadData();
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "取消收藏失败" });
      }
    };

    loadData();

    return { items, loading, removeTopic, viewTopic };
  },
};
</script>

<style scoped lang="less">
.loading-list,
.topic-list {
  display: grid;
  gap: 16px;
}

.topic-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s, transform 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.cover {
  width: 220px;
  height: 140px;
  object-fit: cover;
  border-radius: 6px;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eefbf7, #f8fcfb);
  color: @xtxColor;
  font-size: 22px;
  font-weight: 500;
  border: 1px solid #e0f5ed;
}

.content {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

strong {
  display: block;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.summary {
  margin-top: 8px;
  color: #666;
  line-height: 1.8;
  font-size: 14px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
  color: #999;
  font-size: 13px;

  li {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

@media (max-width: 900px) {
  .topic-card,
  .content {
    grid-template-columns: 1fr;
    display: grid;
  }

  .cover {
    width: 100%;
  }

  .card-actions {
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>
