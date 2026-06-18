<template>
  <footer class="login-footer">
    <div class="container">
      <p>
        <RouterLink to="/about">关于我们</RouterLink>
        <RouterLink to="/help">帮助中心</RouterLink>
        <RouterLink to="/member/after-sale">售后服务</RouterLink>
        <RouterLink :to="{ path: '/help', query: { section: 'shipping' } }">配送与验收</RouterLink>
        <RouterLink :to="{ path: '/about', query: { section: 'cooperation' } }">商务合作</RouterLink>
        <RouterLink :to="{ path: '/help', query: { section: 'shopping' } }">搜索推荐</RouterLink>
        <RouterLink :to="{ path: '/about', query: { section: 'links' } }">友情链接</RouterLink>
      </p>
      <p>CopyRight &copy; 小兔鲜</p>
    </div>
  </footer>
</template>

<script>
export default {
  name: "LoginFooter",
};
</script>

<style scoped lang="less">
.login-footer {
  padding: 30px 0 50px;
  background: #fff;

  p {
    text-align: center;
    color: #999;
    padding-top: 20px;

    a {
      line-height: 1;
      padding: 0 10px;
      color: #999;
      display: inline-block;

      ~ a {
        border-left: 1px solid #ccc;
      }
    }
  }
}
</style>
