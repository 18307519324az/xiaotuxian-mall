<template>
  <MemberFeatureShell title="关注的品牌" summary="品牌关注列表已接入真实接口，并与品牌详情页联动。">
    <div v-if="loading" class="loading-list">
      <XtxSkeleton v-for="i in 3" :key="i" animated bg="#e4e4e4" width="100%" height="160px" />
    </div>
    <div v-else class="brand-list">
      <article class="brand-card portal-card">
        <div class="cover placeholder">品牌馆</div>
        <div class="content">
          <div>
            <strong>前往品牌馆</strong>
            <p class="summary">进入品牌馆查看完整品牌列表和对应的品牌详情。</p>
          </div>
          <div class="card-actions">
            <XtxButton plain @click="$router.push('/brand')">前往品牌馆</XtxButton>
          </div>
        </div>
      </article>
      <article v-for="item in items" :key="item.id" class="brand-card">
        <img :src="item.picture || item.logo" :alt="item.name" class="cover" />
        <div class="content">
          <div>
            <strong>{{ item.name }}</strong>
            <p class="summary">{{ item.desc }}</p>
            <div class="tags">
              <span v-for="tag in item.serviceTags || []" :key="tag">{{ tag }}</span>
            </div>
          </div>
          <div class="card-actions">
            <XtxButton type="primary" plain @click="viewBrand(item)">查看品牌</XtxButton>
            <XtxButton type="gray" plain @click="removeBrand(item)">取消关注</XtxButton>
          </div>
        </div>
      </article>
      <div v-if="items.length === 0" class="empty-tip">暂无关注品牌，去品牌馆挑选感兴趣的品牌。</div>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { ref } from "vue";
import { useRouter } from "vue-router";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { deleteBrandFollow, findBrandFollow } from "@/api/member";

export default {
  name: "MemberBrandFollowPage",
  components: { MemberFeatureShell },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const items = ref([]);

    const loadData = async () => {
      loading.value = true;
      try {
        const data = await findBrandFollow({ page: 1, pageSize: 20 });
        items.value = data.result?.items || [];
      } catch (err) {
        items.value = [];
        Message({ type: "error", text: err.response?.data?.msg || "加载品牌关注失败" });
      } finally {
        loading.value = false;
      }
    };

    const viewBrand = (item) => {
      router.push(`/brand/${item.id}`);
    };

    const removeBrand = async (item) => {
      try {
        await deleteBrandFollow(item.id);
        Message({ type: "success", text: "已取消关注品牌" });
        await loadData();
      } catch (err) {
        Message({ type: "error", text: err.response?.data?.msg || "取消关注失败" });
      }
    };

    loadData();

    return { items, loading, removeBrand, viewBrand };
  },
};
</script>

<style scoped lang="less">
.loading-list,
.brand-list {
  display: grid;
  gap: 16px;
}

.brand-card {
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
  font-size: 24px;
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

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;

  span {
    padding: 4px 10px;
    background: #f2fbf9;
    color: @xtxColor;
    border-radius: 999px;
    font-size: 12px;
  }
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

@media (max-width: 900px) {
  .brand-card,
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
