<template>
  <MemberFeatureShell title="关于我们">
    <div class="about-layout">
      <section
        v-for="item in sections"
        :id="item.id"
        :key="item.id"
        class="about-section"
      >
        <h3>{{ item.title }}</h3>
        <p v-for="text in item.paragraphs" :key="text">{{ text }}</p>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { nextTick, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import MemberFeatureShell from "@/components/member-feature-shell.vue";

const sections = [
  {
    id: "project",
    title: "项目定位",
    paragraphs: [
      "当前项目聚焦小兔鲜 PC 端会员中心与核心购物链路演示，基于 Mock 服务提供联调与验收能力。",
      "v0.9.5 重点完成会员中心风格统一、未完成入口收敛和帮助中心可用性补齐。",
    ],
  },
  {
    id: "capability",
    title: "当前能力",
    paragraphs: [
      "商品详情、购物车、结算、支付、订单、收藏、足迹和猜你喜欢等主链路已完成基线冻结。",
      "会员中心剩余功能已补齐为可演示页面，支持消息、优惠券、积分、礼品卡、评价、售后、邀请和抽奖。",
    ],
  },
  {
    id: "cooperation",
    title: "商务合作",
    paragraphs: [
      "如需对接更多营销、客服或移动端能力，可基于当前 Mock 页面继续扩展真实接口。",
      "现阶段页面说明仅用于演示，不直接承诺真实履约、开票或资金结算。",
    ],
  },
  {
    id: "links",
    title: "友情链接",
    paragraphs: [
      "演示环境不接入外部友情链接平台，当前页面作为站内说明页使用。",
      "如需查看更多信息，可返回帮助中心或会员中心继续操作。",
    ],
  },
];

export default {
  name: "AboutPage",
  components: { MemberFeatureShell },
  setup() {
    const route = useRoute();

    const scrollToSection = async (id) => {
      await nextTick();
      const target = document.getElementById(id);
      if (target) {
        target.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    };

    watch(
      () => route.query.section,
      (value) => {
        if (typeof value === "string") {
          scrollToSection(value);
        }
      }
    );

    onMounted(() => {
      if (typeof route.query.section === "string") {
        scrollToSection(route.query.section);
      }
    });

    return { sections };
  },
};
</script>

<style scoped lang="less">
.about-layout {
  display: grid;
  gap: 16px;
}

.about-section {
  padding: 24px 28px;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.about-section h3 {
  margin-bottom: 14px;
  font-size: 17px;
  font-weight: 600;
  color: #333;
  position: relative;
  padding-left: 14px;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 5px;
    bottom: 5px;
    width: 3px;
    background: @xtxColor;
    border-radius: 2px;
  }
}

.about-section p {
  margin-bottom: 10px;
  color: #555;
  line-height: 1.9;
  font-size: 14px;
}

.about-section p:last-child {
  margin-bottom: 0;
}
</style>
