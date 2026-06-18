<template>
  <MemberFeatureShell title="手机版说明页" summary="提供移动端体验说明、扫码入口和当前演示范围提示。">
    <div class="panel-grid">
      <section class="panel">
        <h3>移动端体验</h3>
        <p>扫码后可进入移动端说明页，用于展示会员服务、活动入口和订单服务的移动端规划。</p>
        <div class="qr-box">
          <img src="@/assets/images/qrcode.jpg" alt="手机版二维码" />
        </div>
      </section>
      <section class="panel">
        <h3>当前说明</h3>
        <ul class="intro-list">
          <li>本页用于替代原先顶部和页脚中的下载死链接入口。</li>
          <li>移动端下载、活动领取和客服咨询均以说明展示为主。</li>
          <li>当前版本不接入真实安装包下载或支付能力。</li>
        </ul>
      </section>
    </div>
  </MemberFeatureShell>
</template>

<script>
import MemberFeatureShell from "@/components/member-feature-shell.vue";

export default {
  name: "MobilePage",
  components: { MemberFeatureShell },
};
</script>

<style scoped lang="less">
.panel-grid {
  gap: 20px;
}

.panel {
  border-radius: 6px;
  padding: 24px 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  h3 {
    margin-bottom: 16px;
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

.qr-box {
  margin-top: 18px;
  width: 160px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #eee;
  background: #fafafa;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.qr-box img {
  display: block;
  width: 100%;
}

.intro-list li,
p {
  color: #555;
  line-height: 1.9;
  font-size: 14px;
}

.intro-list li {
  padding: 8px 0;
  position: relative;
  padding-left: 14px;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 16px;
    width: 5px;
    height: 5px;
    background: @xtxColor;
    border-radius: 50%;
  }
}
</style>
