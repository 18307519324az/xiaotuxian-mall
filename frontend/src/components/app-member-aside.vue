<template>
  <div class="xtx-member-aside">
    <div class="user-manage">
      <h4>我的账户</h4>
      <div class="links">
        <RouterLink to="/member" exact-active-class="active">个人中心</RouterLink>
        <RouterLink to="/member/message" active-class="active">消息通知</RouterLink>
        <RouterLink to="/member/info" active-class="active">个人信息</RouterLink>
        <RouterLink to="/member/security" active-class="active">安全设置</RouterLink>
        <RouterLink to="/member/address" active-class="active">地址管理</RouterLink>
        <RouterLink to="/member/points" active-class="active">我的积分</RouterLink>
        <RouterLink to="/member/history" active-class="active">我的足迹</RouterLink>
        <RouterLink to="/member/invite" active-class="active">邀请有礼</RouterLink>
        <RouterLink to="/member/lottery" active-class="active">幸运抽奖</RouterLink>
      </div>
      <h4>交易管理</h4>
      <div class="links">
        <RouterLink to="/member/order" active-class="active">我的订单</RouterLink>
        <RouterLink to="/member/coupon" active-class="active">优惠券</RouterLink>
        <RouterLink to="/member/gift-card" active-class="active">礼品卡</RouterLink>
        <RouterLink to="/member/review" active-class="active">评价晒单</RouterLink>
        <RouterLink to="/member/after-sale" active-class="active">售后服务</RouterLink>
      </div>
      <h4>我的收藏</h4>
      <div class="links">
        <RouterLink to="/member/collect" active-class="active">收藏的商品</RouterLink>
        <RouterLink to="/member/topic-collect" active-class="active">收藏的专题</RouterLink>
        <RouterLink to="/member/brand-follow" active-class="active">关注的品牌</RouterLink>
      </div>
      <h4>帮助中心</h4>
      <div class="links">
        <RouterLink to="/help" active-class="active">帮助中心</RouterLink>
        <RouterLink to="/member/customer-service" active-class="active">在线客服</RouterLink>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "XtxMemberAside",
};
</script>

<style scoped lang="less">
.xtx-member-aside {
  width: 220px;
  margin-right: 20px;
  border-radius: 2px;

  .user-manage {
    background-color: #fff;

    h4 {
      font-size: 18px;
      font-weight: 400;
      padding: 20px 52px 5px;
      border-top: 1px solid #f6f6f6;
    }

    .links {
      padding: 0 52px 10px;
    }

    a {
      display: block;
      line-height: 1;
      padding: 15px 0;
      font-size: 14px;
      color: #666;
      position: relative;

      &:hover {
        color: @xtxColor;
      }

      &.active {
        color: @xtxColor;

        &:before {
          display: block;
        }
      }

      &:before {
        content: "";
        display: none;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        position: absolute;
        top: 19px;
        left: -16px;
        background-color: @xtxColor;
      }
    }
  }
}
</style>
