<template>
  <div class="search-page">
    <div class="container">
      <!-- 面包屑 -->
      <XtxBread>
        <XtxBreadItem to="/">首页</XtxBreadItem>
        <Transition name="fade-right" mode="out-in">
          <XtxBreadItem :key="keyword">搜索"{{ keyword }}"</XtxBreadItem>
        </Transition>
      </XtxBread>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-input">
          <i class="iconfont icon-search" @click="doSearch"></i>
          <input
            v-model="searchText"
            type="text"
            placeholder="搜一搜"
            @keydown.enter="doSearch"
          />
        </div>
        <XtxButton type="primary" size="small" @click="doSearch">搜索</XtxButton>
      </div>

      <!-- 搜索结果 -->
      <div class="search-result" v-if="!loading">
        <!-- 有结果 -->
        <template v-if="goods.length > 0">
          <div class="result-head">
            <span class="count">共找到 <em>{{ totalCount }}</em> 件商品</span>
          </div>
          <div class="goods-list">
            <GoodsItem v-for="item in goods" :key="item.id" :goods="item" />
          </div>
          <!-- 分页 -->
          <XtxPagination
            v-if="totalPages > 1"
            :current-page="currentPage"
            :total="totalCount"
            :page-size="pageSize"
            @current-change="changePage"
          />
        </template>
        <!-- 无结果 -->
        <template v-else>
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <p class="empty-text">未找到与"<em>{{ keyword }}</em>"相关的商品</p>
            <p class="empty-hint">试试其他关键词，或浏览分类推荐</p>
          </div>
        </template>
      </div>

      <!-- 加载中 -->
      <div class="loading-state" v-else>
        <XtxSkeleton width="100%" height="200px" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { searchGoods } from "@/api/search";
import GoodsItem from "@/views/category/components/goods-item";

export default {
  name: "SearchPage",
  components: { GoodsItem },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const keyword = ref("");
    const searchText = ref("");
    const goods = ref([]);
    const totalCount = ref(0);
    const currentPage = ref(1);
    const pageSize = ref(20);
    const totalPages = ref(0);
    const loading = ref(false);

    // 执行搜索
    const fetchSearch = async (kw, page) => {
      if (!kw || !kw.trim()) {
        goods.value = [];
        totalCount.value = 0;
        totalPages.value = 0;
        return;
      }
      loading.value = true;
      try {
        const data = await searchGoods(kw, page, pageSize.value);
        const result = data.result || {};
        goods.value = result.items || [];
        totalCount.value = result.counts || 0;
        currentPage.value = result.page || 1;
        totalPages.value = result.pages || 0;
      } catch (e) {
        console.error("搜索失败", e);
        goods.value = [];
        totalCount.value = 0;
        totalPages.value = 0;
        // 如果之前有搜索结果，不覆盖错误提示；静默恢复到空状态
      } finally {
        loading.value = false;
      }
    };

    // 从路由参数读取关键词
    const loadFromRoute = () => {
      const kw = (route.query.keyword || "").trim();
      keyword.value = kw;
      searchText.value = kw;
      currentPage.value = 1;
      fetchSearch(kw, 1);
    };

    // 执行搜索（从搜索栏触发）
    const doSearch = () => {
      const kw = searchText.value.trim();
      router.replace({ path: "/search", query: { keyword: kw } });
    };

    // 翻页
    const changePage = (page) => {
      currentPage.value = page;
      fetchSearch(keyword.value, page);
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // 路由参数变化时重新搜索
    watch(
      () => route.query.keyword,
      (newKw) => {
        if (newKw !== keyword.value) {
          loadFromRoute();
        }
      }
    );

    onMounted(loadFromRoute);

    return {
      keyword,
      searchText,
      goods,
      totalCount,
      currentPage,
      pageSize,
      totalPages,
      loading,
      doSearch,
      changePage,
    };
  },
};
</script>

<style scoped lang="less">
.search-page {
  min-height: 500px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 20px 0;
  padding: 20px;
  background: #fff;
  border-radius: 4px;

  .search-input {
    flex: 1;
    display: flex;
    align-items: center;
    border: 1px solid #e7e7e7;
    border-radius: 4px;
    padding: 8px 12px;

    .icon-search {
      font-size: 18px;
      color: #999;
      cursor: pointer;
      margin-right: 8px;

      &:hover {
        color: @xtxColor;
      }
    }

    input {
      flex: 1;
      border: none;
      outline: none;
      font-size: 14px;
      color: #333;

      &::placeholder {
        color: #ccc;
      }
    }
  }
}

.result-head {
  padding: 10px 0;
  font-size: 14px;
  color: #999;

  em {
    color: @xtxColor;
    font-style: normal;
    font-weight: bold;
  }
}

.goods-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  background: #fff;
  border-radius: 4px;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 18px;
    color: #333;
    margin-bottom: 8px;

    em {
      color: @xtxColor;
      font-style: normal;
    }
  }

  .empty-hint {
    font-size: 14px;
    color: #999;
  }
}

.loading-state {
  padding: 20px;
}
</style>
