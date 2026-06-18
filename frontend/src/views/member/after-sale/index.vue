<template>
  <MemberFeatureShell eyebrow="Service" title="售后服务" summary="支持查看售后记录，并为订单商品提交退款、换货或维修申请。">
    <div class="panel-grid">
      <section class="panel">
        <h3>发起售后申请</h3>
        <div class="form-grid">
          <label>
            <span class="label-title">订单商品</span>
            <select v-model="form.selection">
              <option value="">请选择售后商品</option>
              <option v-for="item in candidates" :key="`${item.orderId}-${item.skuId}`" :value="`${item.orderId}|${item.skuId}`">
                {{ candidateLabel(item) }}
              </option>
            </select>
          </label>
          <label>
            <span class="label-title">服务类型</span>
            <select v-model="form.type">
              <option v-for="item in serviceTypes" :key="item" :value="item">{{ typeLabelMap[item] || item }}</option>
            </select>
          </label>
          <label>
            <span class="label-title">售后原因</span>
            <select v-model="form.reason">
              <option v-for="item in reasons" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
          <label class="full">
            <span class="label-title">问题描述</span>
            <textarea v-model.trim="form.description" placeholder="补充描述商品问题、诉求或图片说明" />
          </label>
        </div>
        <XtxButton type="primary" data-test="after-sale-submit-btn" @click="submit">提交售后申请</XtxButton>
      </section>

      <section class="panel">
        <h3>售后记录</h3>
        <div v-if="records.length === 0" class="empty-tip">当前还没有售后记录。</div>
        <article v-for="item in records" :key="item.id" class="service-item">
          <div>
            <strong>{{ item.orderId }} · {{ typeLabelMap[item.type] || item.type }}</strong>
            <p>{{ item.reason }}</p>
            <p>{{ item.description || "已提交售后说明" }}</p>
            <small>{{ item.createdAt }}</small>
          </div>
          <div class="service-meta">
            <span>{{ statusLabelMap[item.status] || item.status }}</span>
          </div>
        </article>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import { onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import MemberFeatureShell from "@/components/member-feature-shell.vue";
import Message from "@/components/library/Message";
import { findAfterSales, submitAfterSale } from "@/api/member";

const typeLabelMap = {
  refund: "退款",
  exchange: "换货",
  repair: "维修",
};

const statusLabelMap = {
  submitted: "已提交",
  processing: "处理中",
  completed: "已完成",
};

export default {
  name: "MemberAfterSalePage",
  components: { MemberFeatureShell },
  setup() {
    const route = useRoute();
    const records = ref([]);
    const candidates = ref([]);
    const serviceTypes = ref([]);
    const reasons = ref([]);
    const form = reactive({
      selection: "",
      type: "refund",
      reason: "包装破损",
      description: "",
    });

    const candidateLabel = (item) => {
      const attrs = item.attrsText ? ` · ${item.attrsText}` : "";
      const quantity = item.quantity ? ` ×${item.quantity}` : "";
      return `${item.orderId} · ${item.name}${attrs}${quantity}`;
    };

    const load = async () => {
      const params = route.query.orderId ? { orderId: route.query.orderId } : {};
      const data = await findAfterSales(params);
      records.value = data.result.items || [];
      candidates.value = data.result.candidates || [];
      serviceTypes.value = data.result.serviceTypes || [];
      reasons.value = data.result.reasons || [];
      if (!form.selection && route.query.orderId) {
        const first = candidates.value.find((item) => item.orderId === route.query.orderId);
        if (first) {
          form.selection = `${first.orderId}|${first.skuId}`;
        }
      }
    };

    const submit = async () => {
      if (!form.selection) {
        Message({ type: "warn", text: "请选择售后商品" });
        return;
      }
      const [orderId, skuId] = form.selection.split("|");
      await submitAfterSale({
        orderId,
        skuId,
        type: form.type,
        reason: form.reason,
        description: form.description,
      });
      Message({ type: "success", text: "售后申请已提交" });
      form.description = "";
      await load();
    };

    watch(() => route.query.orderId, load);
    onMounted(load);

    return { records, candidates, serviceTypes, reasons, form, submit, candidateLabel, typeLabelMap, statusLabelMap };
  },
};
</script>

<style scoped lang="less">
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

label {
  display: block;
}

.label-title {
  display: block;
  color: #333;
}

.form-grid .full {
  grid-column: 1 / -1;
}

select,
textarea {
  width: 100%;
  margin-top: 8px;
  padding: 10px 12px;
  border: 1px solid #d8dee6;
  border-radius: 12px;
  box-sizing: border-box;
}

textarea {
  min-height: 110px;
  resize: vertical;
}

.service-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eef0f3;
}

.service-item:last-child {
  border-bottom: none;
}

.service-item p,
.service-item small {
  color: #6a7280;
  line-height: 1.7;
}

.service-meta {
  min-width: 88px;
  text-align: right;
}

.service-meta span {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f2fbf9;
  color: @xtxColor;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
