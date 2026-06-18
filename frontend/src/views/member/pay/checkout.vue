<template>
  <div class="xtx-pay-checkout-page">
    <div class="container">
      <XtxBread>
        <XtxBreadItem to="/">首页</XtxBreadItem>
        <XtxBreadItem to="/cart">购物车</XtxBreadItem>
        <XtxBreadItem>填写订单</XtxBreadItem>
      </XtxBread>
      <div class="wrapper" v-if="order">
        <!-- 收货地址 -->
        <h3 class="box-title">收货地址</h3>
        <div class="box-body">
          <!-- 收货地址组件 -->
          <CheckoutAddress @change="changeAddress" :list="order.userAddresses" />
        </div>
        <!-- 商品信息 -->
        <h3 class="box-title">商品信息</h3>
        <div class="box-body">
          <table class="goods">
            <thead>
              <tr>
                <th width="520">商品信息</th>
                <th width="170">单价</th>
                <th width="170">数量</th>
                <th width="170">小计</th>
                <th width="170">实付</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in order.goods" :key="item.skuId">
                <td>
                  <a href="javascript:;" class="info">
                    <img :src="item.picture" alt="" />
                    <div class="right">
                      <p>{{ item.name }}</p>
                      <p>{{ formatAttrsText(item.attrsText) }}</p>
                    </div>
                  </a>
                </td>
                <td>&yen;{{ item.price }}</td>
                <td>{{ item.count }}</td>
                <td>&yen;{{ item.totalPrice }}</td>
                <td>&yen;{{ item.totalPayPrice }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 配送时间 -->
        <h3 class="box-title">配送时间</h3>
        <div class="box-body">
          <a class="my-btn" :class="{ active: deliveryTimeType === 1 }" href="javascript:;" @click="selectDeliveryTime(1)">不限送货时间：周一至周日</a>
          <a class="my-btn" :class="{ active: deliveryTimeType === 2 }" href="javascript:;" @click="selectDeliveryTime(2)">工作日送货：周一至周五</a>
          <a class="my-btn" :class="{ active: deliveryTimeType === 3 }" href="javascript:;" @click="selectDeliveryTime(3)">双休日、假日送货：周六至周日</a>
        </div>
        <!-- 支付方式 -->
        <h3 class="box-title">支付方式</h3>
        <div class="box-body">
          <a class="my-btn" :class="{ active: payType === 1 }" href="javascript:;" @click="selectPayType(1)">在线支付</a>
          <a class="my-btn" :class="{ active: payType === 2 }" href="javascript:;" @click="selectPayType(2)">货到付款</a>
          <span style="color: #999">货到付款需付5元手续费</span>
        </div>
        <!-- 优惠券 -->
        <h3 class="box-title">优惠券
          <span v-if="selectedCouponId" class="cancel-link" @click.stop="cancelCoupon">取消使用</span>
        </h3>
        <div class="box-body">
          <div class="coupon-list" v-if="sortedCoupons.length > 0">
            <label class="coupon-item" v-for="c in sortedCoupons" :key="c.id"
              :class="{ selected: selectedCouponId === c.id, disabled: (c.minPrice || c.threshold) && +summaryTotalPrice < (c.minPrice || c.threshold) }"
              @click="toggleCoupon(c)">
              <div class="coupon-info">
                <span class="coupon-name">{{ c.name }}</span>
                <span class="coupon-desc">{{ c.description }}</span>
                <span class="coupon-amount">-¥{{ c.amount }}</span>
                <span v-if="(c.minPrice || c.threshold) && +summaryTotalPrice < (c.minPrice || c.threshold)" class="coupon-disabled-tip">满¥{{ c.minPrice || c.threshold }}可用</span>
              </div>
            </label>
          </div>
          <div v-else>暂无可用优惠券</div>
        </div>
        <!-- 礼品卡 -->
        <h3 class="box-title">礼品卡
          <span v-if="selectedGiftCardCode" class="cancel-link" @click.stop="cancelGiftCard">取消使用</span>
        </h3>
        <div class="box-body">
          <div class="gift-card-list" v-if="giftCards.length > 0">
            <label class="gift-card-item" v-for="card in giftCards" :key="card.id"
              :class="{ selected: selectedGiftCardCode === card.code, disabled: card.balance <= 0 }"
              @click="toggleGiftCard(card)">
              <div class="card-info">
                <span class="card-name">{{ card.name }}</span>
                <span class="card-code">{{ card.code }}</span>
                <span class="card-balance">余额 ¥{{ card.balance }}</span>
              </div>
            </label>
          </div>
          <div class="gift-card-bind">
            <input v-model="giftCardBindCode" placeholder="输入礼品卡卡号" />
            <XtxButton type="plain" size="small" @click="bindGiftCardFn">绑定</XtxButton>
          </div>
        </div>
        <!-- 金额明细 -->
        <h3 class="box-title">金额明细</h3>
        <div class="box-body">
          <div class="total">
            <dl>
              <dt>商品金额：</dt>
              <dd>¥{{ summaryTotalPrice }}</dd>
            </dl>
            <dl>
              <dt>运费：</dt>
              <dd>¥{{ summaryPostFee }}</dd>
            </dl>
            <dl v-if="estimatedGoodsDiscount > 0">
              <dt>商品优惠：</dt>
              <dd class="discount">-¥{{ estimatedGoodsDiscount.toFixed(2) }}</dd>
            </dl>
            <dl v-if="estimatedFreightDiscount > 0">
              <dt>运费优惠：</dt>
              <dd class="discount">-¥{{ estimatedFreightDiscount.toFixed(2) }}</dd>
            </dl>
            <dl v-if="giftCardDeduction > 0">
              <dt>礼品卡抵扣：</dt>
              <dd class="discount">-¥{{ giftCardDeduction }}</dd>
            </dl>
            <dl v-if="selectedGiftCardCode && giftCardDeduction > 0">
              <dt>使用礼品卡：</dt>
              <dd>{{ selectedGiftCardCode }}</dd>
            </dl>
            <dl>
              <dt>应付总额：</dt>
              <dd class="price">¥{{ finalPayPrice }}</dd>
            </dl>
          </div>
        </div>
        <!-- 提交订单 -->
        <div class="submit">
          <XtxButton @click="submitOrderFn" type="primary" :disabled="submitDisabled">提交订单</XtxButton>
          <p v-if="goodsEmpty" class="empty-tip">没有可结算的商品，请先去购物车选择商品</p>
          <p v-else-if="zeroPrice" class="empty-tip">再次购买商品信息异常，请返回订单重新操作</p>
        </div>
      </div>
      <!-- 空商品提示 -->
      <div v-else class="wrapper-empty">
        <p class="empty-msg">订单加载中...</p>
      </div>
    </div>
  </div>
</template>
<script>
import CheckoutAddress from "./components/checkout-address";
import { createOrder, submitOrder, repurchaseOrder } from "@/api/order";
import { findGiftCards, bindGiftCard } from "@/api/member";
import { formatAttrsText } from "@/utils/format";
import { reactive, ref, computed } from "vue";
import Message from "@/components/library/Message";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";
export default {
  name: "XtxPayCheckoutPage",
  components: { CheckoutAddress },
  setup() {
    // 结算功能-生成订单-订单信息
    const store = useStore();
    const order = ref(null);
    const route = useRoute();

    // 判断是否为运费类优惠券（运费券 / 包邮券）
    const isFreightCouponType = (couponType) => {
      return ['freight', 'shipping', 'freeShipping', '运费券', '包邮券'].includes(couponType);
    };

    // 排序优惠券：运费券排前面，方便用户优先使用
    const sortedCoupons = computed(() => {
      if (!order.value?.coupons) return [];
      return [...order.value.coupons].sort((a, b) => {
        const aIsFreight = isFreightCouponType(a.couponType) ? 0 : 1;
        const bIsFreight = isFreightCouponType(b.couponType) ? 0 : 1;
        return aIsFreight - bIsFreight;
      });
    });

    // 优惠券选择
    const selectedCouponId = ref(null);

    // 礼品卡 — 单卡选择模式
    const giftCards = ref([]);
    const giftCardBindCode = ref("");
    const selectedGiftCardCode = ref(null);

    const loadGiftCards = () => {
      findGiftCards().then(data => {
        const result = data.result;
        const activeCards = result.cards?.filter(c => c.status === "active" && c.balance > 0) || [];
        giftCards.value = activeCards;
        // 默认选择第一张有余额的卡
        if (activeCards.length > 0 && !selectedGiftCardCode.value) {
          selectedGiftCardCode.value = activeCards[0].code;
        }
      }).catch(() => {});
    };

    const bindGiftCardFn = () => {
      if (!giftCardBindCode.value) return Message({ text: "请输入礼品卡卡号" });
      bindGiftCard(giftCardBindCode.value).then(() => {
        Message({ type: "success", text: "绑定成功" });
        giftCardBindCode.value = "";
        loadGiftCards();
      }).catch(() => {
        Message({ type: "error", text: "绑定失败" });
      });
    };

    const afterOrderLoad = (data) => {
      order.value = data.result;
      reqParams.goods = data.result.goods.map(({ skuId, count }) => ({ skuId, count }));
      loadGiftCards();
    };

    if (route.query.orderId) {
      repurchaseOrder(route.query.orderId).then(afterOrderLoad);
    } else {
      createOrder().then(afterOrderLoad);
    }

    // 接收收货地址ID
    const changeAddress = (id) => {
      reqParams.addressId = id;
    };

    // 配送时间选择
    const deliveryTimeType = ref(1);
    const selectDeliveryTime = (n) => {
      deliveryTimeType.value = n;
      reqParams.deliveryTimeType = n;
    };

    // 支付方式选择
    const payType = ref(1);
    const selectPayType = (n) => {
      payType.value = n;
      reqParams.payType = n;
    };

    // ========== 实时金额计算 ==========

    // 商品总价
    const summaryTotalPrice = computed(() => {
      if (!order.value) return "0.00";
      return parseFloat(order.value.summary.totalPrice || "0").toFixed(2);
    });

    // 运费
    const summaryPostFee = computed(() => {
      if (!order.value) return "0.00";
      const base = parseFloat(order.value.summary.postFee || "0");
      return (payType.value === 2 ? base + 5 : base).toFixed(2);
    });

    // 选中优惠券的预估商品抵扣金额：商品券才抵扣商品，运费券不影响商品金额
    const estimatedGoodsDiscount = computed(() => {
      if (!selectedCouponId.value || !order.value?.coupons) return 0;
      const c = order.value.coupons.find(c => c.id === selectedCouponId.value);
      if (!c) return 0;
      const amount = parseFloat(c.amount || "0");
      if (isFreightCouponType(c.couponType)) return 0; // 运费券不影响商品金额
      if (c.couponType === "goods") {
        const totalPrice = parseFloat(summaryTotalPrice.value || "0");
        return Math.min(amount, totalPrice);
      }
      return 0;
    });

    // 选中优惠券的预估运费抵扣金额：运费券才抵扣运费，商品券不影响运费
    const estimatedFreightDiscount = computed(() => {
      if (!selectedCouponId.value || !order.value?.coupons) return 0;
      const c = order.value.coupons.find(c => c.id === selectedCouponId.value);
      if (!c) return 0;
      const amount = parseFloat(c.amount || "0");
      if (isFreightCouponType(c.couponType)) {
        const postFee = parseFloat(summaryPostFee.value || "0");
        return Math.min(amount, postFee); // 运费券最多抵扣运费金额
      }
      if (c.couponType === "goods") return 0; // 商品券不影响运费
      return 0;
    });

    // 选中礼品卡的余额
    const selectedGiftCardBalance = computed(() => {
      if (!selectedGiftCardCode.value) return 0;
      const card = giftCards.value.find(c => c.code === selectedGiftCardCode.value);
      return card ? parseFloat(card.balance || "0") : 0;
    });

    // 礼品卡抵扣金额 = min(选中卡余额, 优惠券后应付金额)
    const giftCardDeduction = computed(() => {
      if (!selectedGiftCardCode.value || selectedGiftCardBalance.value <= 0) return 0;
      const totalPrice = parseFloat(summaryTotalPrice.value || "0");
      const postFee = parseFloat(summaryPostFee.value || "0");
      const afterCoupon = Math.max(0, totalPrice + postFee - estimatedGoodsDiscount.value - estimatedFreightDiscount.value);
      return Math.min(selectedGiftCardBalance.value, afterCoupon);
    });

    // 最终应付金额（不小于 0）
    const finalPayPrice = computed(() => {
      const totalPrice = parseFloat(summaryTotalPrice.value || "0");
      const postFee = parseFloat(summaryPostFee.value || "0");
      return Math.max(0, totalPrice + postFee - estimatedGoodsDiscount.value - estimatedFreightDiscount.value - giftCardDeduction.value).toFixed(2);
    });

    // 商品是否为空
    const goodsEmpty = computed(() => {
      return !order.value || !order.value.goods || order.value.goods.length === 0;
    });

    // 金额是否为零（允许 0 元提交）
    const zeroPrice = computed(() => {
      if (!order.value) return false;
      return parseFloat(finalPayPrice.value) <= 0;
    });

    // 按钮是否禁用（0 元也可提交）
    const submitDisabled = computed(() => goodsEmpty.value);

    // 结算功能-提交订单-提交信息
    const reqParams = reactive({
      deliveryTimeType: 1,
      payType: 1,
      payChannel: 1,
      buyerMessage: "",
      goods: [],
      addressId: null,
    });

    // 提交订单
    const router = useRouter();
    const submitOrderFn = () => {
      if (!reqParams.addressId) {
        return Message({ text: "亲，请选择收货地址" });
      }
      if (!order.value || !order.value.goods || order.value.goods.length === 0) {
        return Message({ type: "warn", text: "没有可结算的商品" });
      }
      // 附加优惠券和礼品卡参数
      if (selectedCouponId.value) {
        reqParams.couponId = selectedCouponId.value;
      }
      if (selectedGiftCardCode.value) {
        reqParams.giftCardCode = selectedGiftCardCode.value;
      }
      const payPrice = parseFloat(finalPayPrice.value);
      submitOrder(reqParams).then((data) => {
        Message({ type: "success", text: "提交订单成功" });
        store.dispatch("cart/findCart");
        if (payType.value === 1 && payPrice > 0) {
          // 在线支付且金额>0 → 去支付页
          router.push(`/member/pay?orderId=${data.result.id}`);
        } else {
          // 货到付款或 0 元 → 订单详情
          router.push(`/member/order/${data.result.id}`);
        }
      });
    };

    // 优惠券 toggle：选中或取消
    const toggleCoupon = (c) => {
      if (selectedCouponId.value === c.id) {
        selectedCouponId.value = null;
      } else {
        const minPrice = c.minPrice || c.threshold;
        if (minPrice && +summaryTotalPrice.value < minPrice) return;
        selectedCouponId.value = c.id;
      }
    };

    // 取消优惠券
    const cancelCoupon = () => {
      selectedCouponId.value = null;
    };

    // 礼品卡 toggle：选中或取消
    const toggleGiftCard = (card) => {
      if (selectedGiftCardCode.value === card.code) {
        selectedGiftCardCode.value = null;
      } else {
        if (card.balance <= 0) return;
        selectedGiftCardCode.value = card.code;
      }
    };

    // 取消礼品卡
    const cancelGiftCard = () => {
      selectedGiftCardCode.value = null;
    };

    return { order, sortedCoupons, changeAddress, deliveryTimeType, payType, selectDeliveryTime, selectPayType, summaryTotalPrice, summaryPostFee, estimatedGoodsDiscount, estimatedFreightDiscount, selectedCouponId, giftCardDeduction, finalPayPrice, giftCards, selectedGiftCardCode, selectedGiftCardBalance, giftCardBindCode, bindGiftCardFn, goodsEmpty, zeroPrice, submitDisabled, submitOrderFn, formatAttrsText, toggleCoupon, cancelCoupon, toggleGiftCard, cancelGiftCard };
  },
};
</script>
<style scoped lang="less">
.xtx-pay-checkout-page {
  .wrapper {
    background: #fff;
    padding: 0 20px;
    .box-title {
      font-size: 16px;
      font-weight: normal;
      padding-left: 10px;
      line-height: 70px;
      border-bottom: 1px solid #f5f5f5;
    }
    .box-body {
      padding: 20px 0;
    }
  }
}
.goods {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  .info {
    display: flex;
    text-align: left;
    img {
      width: 70px;
      height: 70px;
      margin-right: 20px;
    }
    .right {
      line-height: 24px;
      p {
        &:last-child {
          color: #999;
        }
      }
    }
  }
  tr {
    th {
      background: #f5f5f5;
      font-weight: normal;
    }
    td,
    th {
      text-align: center;
      padding: 20px;
      border-bottom: 1px solid #f5f5f5;
      &:first-child {
        border-left: 1px solid #f5f5f5;
      }
      &:last-child {
        border-right: 1px solid #f5f5f5;
      }
    }
  }
}
.my-btn {
  width: 228px;
  height: 50px;
  border: 1px solid #e4e4e4;
  text-align: center;
  line-height: 48px;
  margin-right: 25px;
  color: #666666;
  display: inline-block;
  &.active,
  &:hover {
    border-color: @xtxColor;
  }
}
.total {
  dl {
    display: flex;
    justify-content: flex-end;
    line-height: 50px;
    dt {
      i {
        display: inline-block;
        width: 2em;
      }
    }
    dd {
      width: 240px;
      text-align: right;
      padding-right: 70px;
      &.price {
        font-size: 20px;
        color: @priceColor;
      }
    }
  }
}
.submit {
  text-align: right;
  padding: 60px;
  border-top: 1px solid #f5f5f5;
  .empty-tip {
    color: #999;
    font-size: 14px;
    margin-top: 10px;
    text-align: right;
  }
}
.wrapper-empty {
  background: #fff;
  padding: 80px 20px;
  text-align: center;
  .empty-msg {
    font-size: 16px;
    color: #999;
  }
}
.coupon-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.coupon-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border: 1px solid #e4e4e4;
  border-radius: 4px;
  cursor: pointer;
  &.selected {
    border-color: @xtxColor;
  }
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  input {
    margin-right: 8px;
  }
}
.coupon-info {
  display: flex;
  flex-direction: column;
}
.coupon-name {
  font-weight: bold;
}
.coupon-desc {
  color: #999;
  font-size: 12px;
}
.coupon-amount {
  color: @priceColor;
  font-weight: bold;
}
.coupon-disabled-tip {
  color: #999;
  font-size: 11px;
}
.gift-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.gift-card-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border: 1px solid #e4e4e4;
  border-radius: 4px;
  cursor: pointer;
  &.selected {
    border-color: @xtxColor;
  }
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  input {
    margin-right: 10px;
  }
}
.card-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.card-name {
  font-weight: bold;
  font-size: 14px;
}
.card-code {
  color: #999;
  font-size: 12px;
}
.card-balance {
  color: @priceColor;
  font-weight: bold;
  font-size: 14px;
}
.gift-card-bind {
  margin-bottom: 12px;
  input {
    width: 200px;
    height: 34px;
    border: 1px solid #e4e4e4;
    border-radius: 4px;
    padding: 0 10px;
    margin-right: 8px;
  }
}
.discount {
  color: @priceColor;
}
.cancel-link {
  font-size: 12px;
  color: #27BA9B;
  cursor: pointer;
  margin-left: 10px;
  font-weight: normal;
}
.cancel-link:hover {
  color: #1e9a82;
}
</style>
