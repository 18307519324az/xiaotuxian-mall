<template>
  <MemberFeatureShell title="帮助中心">
    <div class="help-layout">
      <aside class="quick-card">
        <h3>快捷入口</h3>
        <button
          v-for="item in quickLinks"
          :key="item.id"
          type="button"
          class="quick-link"
          :class="{ active: activeSection === item.id }"
          :data-test="`help-link-${item.id}`"
          @click="openSection(item.id)"
        >
          {{ item.label }}
        </button>
      </aside>

      <div class="content-card">
        <section
          v-for="item in sections"
          :id="item.id"
          :key="item.id"
          class="help-section"
          :data-help-section="item.id"
        >
          <h3>{{ item.title }}</h3>
          <p v-for="text in item.paragraphs" :key="text">{{ text }}</p>
        </section>
      </div>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import MemberFeatureShell from "@/components/member-feature-shell.vue";

const sections = [
  {
    id: "shopping",
    title: "购物流程",
    paragraphs: [
      "首页、分类页和商品详情页均可直接进入购买链路，已加入购物车的商品可在购物车统一结算。",
      "若需要再次购买，可在我的订单中直接进入再次购买结算页，不影响已冻结的交易逻辑。",
    ],
  },
  {
    id: "payment",
    title: "支付说明",
    paragraphs: [
      "当前演示环境支持在线支付与货到付款两种方式，支付页会保留原有支付状态流转。",
      "优惠券、积分、礼品卡页面为会员演示功能，不接入真实订单金额抵扣。",
    ],
  },
  {
    id: "shipping",
    title: "配送说明",
    paragraphs: [
      "地址管理页支持新增、编辑、删除与默认地址切换，配送信息以结算页展示为准。",
      "配送时间支持不限时间、工作日和双休日三个选项，操作结果会直接体现在结算页。",
    ],
  },
  {
    id: "returns",
    title: "退换货说明",
    paragraphs: [
      "已完成订单可从订单列表进入售后服务页，发起退款、换货或维修申请。",
      "售后记录会保存在会员中心售后服务页，用于演示服务流程与状态跟踪。",
    ],
  },
  {
    id: "invoice",
    title: "发票说明",
    paragraphs: [
      "发票信息以订单支付说明页展示为准，当前环境用于页面演示，不接入真实税务系统。",
      "如需统一开票规则，可在商务合作或客服入口中查看项目说明。",
    ],
  },
  {
    id: "points",
    title: "积分与会员权益",
    paragraphs: [
      "会员中心支持查看积分余额、最近积分变动、邀请奖励和抽奖机会。",
      "公开评价会展示当前用户昵称，匿名评价会展示匿名用户。",
    ],
  },
  {
    id: "service",
    title: "联系客服",
    paragraphs: [
      "在线客服页提供热线、服务时间和常见处理入口，可以从会员侧边栏直接进入。",
      "若当前问题需要人工协助，可优先准备订单号、收货信息和问题描述后再联系。",
    ],
  },
];

export default {
  name: "HelpPage",
  components: { MemberFeatureShell },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const activeSection = ref("shopping");
    const sectionOffset = 96;

    const quickLinks = [
      { id: "payment", label: "订单支付与发票说明" },
      { id: "shipping", label: "地址管理与配送范围" },
      { id: "returns", label: "退款、换货与售后流程" },
      { id: "points", label: "会员权益与积分规则" },
    ];

    const waitForRouteScrollReset = () =>
      new Promise((resolve) => {
        requestAnimationFrame(() => {
          requestAnimationFrame(resolve);
        });
      });

    const scrollToSection = async (id) => {
      await nextTick();
      const target = document.getElementById(id);
      if (!target) return;
      activeSection.value = id;
      const maxTop = Math.max(document.documentElement.scrollHeight - window.innerHeight, 0);
      const targetTop = target.getBoundingClientRect().top + window.pageYOffset - sectionOffset;
      window.scrollTo({
        top: Math.min(Math.max(targetTop, 0), maxTop),
        behavior: "auto",
      });
    };

    const openSection = async (id) => {
      activeSection.value = id;
      await router.replace({ path: route.path, query: { section: id } });
      await waitForRouteScrollReset();
      await scrollToSection(id);
    };

    watch(
      () => route.query.section,
      (value) => {
        const targetId = typeof value === "string" ? value : "shopping";
        activeSection.value = targetId;
      }
    );

    onMounted(async () => {
      const targetId = typeof route.query.section === "string" ? route.query.section : "shopping";
      activeSection.value = targetId;
      await waitForRouteScrollReset();
      scrollToSection(targetId);
    });

    return { sections, quickLinks, activeSection, openSection };
  },
};
</script>

<style scoped lang="less">
.help-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.quick-card {
  border-radius: 6px;
  background: #fafafa;
  padding: 20px 16px;
  align-self: start;
  position: sticky;
  top: 20px;

  h3 {
    margin-bottom: 16px;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
  }
}

.quick-link {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  padding: 10px 14px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666;
  text-align: left;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-link.active,
.quick-link:hover {
  color: #fff;
  background: @xtxColor;
}

.content-card {
  border-radius: 6px;
  background: #fff;
  padding: 24px 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.help-section {
  padding: 8px 0 24px;
  border-bottom: 1px solid #f0f0f0;
  scroll-margin-top: 96px;

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }

  h3 {
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
}

.help-section p {
  color: #555;
  line-height: 1.9;
  margin-bottom: 10px;
  font-size: 14px;
  padding-left: 17px;
}

@media (max-width: 900px) {
  .help-layout {
    grid-template-columns: 1fr;
  }

  .quick-card {
    position: static;
  }

  .quick-links {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
