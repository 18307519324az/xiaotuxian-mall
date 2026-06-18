<template>
  <section class="member-feature-page">
    <header class="member-nav feature-nav">
      <div class="title-wrap">
        <span class="title">{{ title }}</span>
        <p v-if="summary" class="summary">{{ summary }}</p>
      </div>
      <div v-if="$slots.actions" class="actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="feature-body">
      <slot />
    </div>
  </section>
</template>

<script>
export default {
  name: "MemberFeatureShell",
  props: {
    title: {
      type: String,
      required: true,
    },
    summary: {
      type: String,
      default: "",
    },
  },
};
</script>

<style scoped lang="less">
.member-feature-page {
  min-height: 420px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.feature-nav {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.title-wrap {
  min-width: 0;
}

.title {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.summary {
  margin-top: 6px;
  max-width: 760px;
  color: #999;
  font-size: 13px;
  line-height: 1.7;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.feature-body {
  padding: 20px 24px 28px;
}

.feature-body:deep(.stats-grid) {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.feature-body:deep(.stat-card) {
  padding: 18px 20px;
  border-radius: 6px;
  background: #fafafa;
}

.feature-body:deep(.stat-card .label) {
  font-size: 13px;
  color: #999;
}

.feature-body:deep(.stat-card .value) {
  margin-top: 10px;
  font-size: 24px;
  line-height: 1.2;
  color: #333;
  font-weight: 600;
}

.feature-body:deep(.panel-grid) {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.feature-body:deep(.panel),
.feature-body:deep(.list-card) {
  padding: 20px;
  border-radius: 6px;
  background: #fff;
}

.feature-body:deep(.panel h3),
.feature-body:deep(.list-card h3) {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.feature-body:deep(.empty-tip) {
  padding: 32px 0;
  text-align: center;
  color: #999;
}

@media (max-width: 900px) {
  .feature-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    justify-content: flex-start;
  }

  .feature-body:deep(.stats-grid),
  .feature-body:deep(.panel-grid) {
    grid-template-columns: 1fr;
  }
}
</style>
