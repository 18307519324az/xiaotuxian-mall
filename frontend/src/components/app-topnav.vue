<template>
  <nav class="app-topnav">
    <div class="container">
      <ul>
        <template v-if="profile.token">
          <li>
            <RouterLink to="/member">
              <img v-if="profile.avatar" :src="profile.avatar" class="top-avatar" />
              <i v-else class="iconfont icon-user"></i>{{ profile.nickname || profile.account }}
            </RouterLink>
          </li>
          <li><button type="button" class="logout-btn" @click="logout()">退出登录</button></li>
        </template>
        <template v-else>
          <li><RouterLink to="/login">请先登录</RouterLink></li>
          <li><RouterLink to="/register">免费注册</RouterLink></li>
        </template>
        <li><RouterLink to="/member/order">我的订单</RouterLink></li>
        <li><RouterLink to="/member">会员中心</RouterLink></li>
        <li><RouterLink to="/help">帮助中心</RouterLink></li>
        <li><RouterLink to="/about">关于我们</RouterLink></li>
        <li>
          <RouterLink to="/mobile"><i class="iconfont icon-phone"></i>手机版</RouterLink>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
import { computed } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { userLogout } from "@/api/member";

export default {
  name: "AppTopnav",
  setup() {
    const store = useStore();
    const router = useRouter();
    const profile = computed(() => store.state.user.profile);
    const logout = () => {
      // 调用后端 logout 接口（异步，不阻塞前端退出）
      userLogout().catch(() => {});
      // 清除 Vuex 状态
      store.commit("user/setUser", {});
      store.commit("cart/setCart", []);
      // 清除 localStorage（Vuex 持久化插件负责，但显式清除确保安全）
      localStorage.removeItem("erabbit-client-pc-store");
      // 跳转登录页
      router.push("/login");
    };
    return { profile, logout };
  },
};
</script>

<style scoped lang="less">
.app-topnav {
  background: #333;
  ul {
    display: flex;
    height: 53px;
    justify-content: flex-end;
    align-items: center;
    li {
      a {
        padding: 0 15px;
        color: #cdcdcd;
        line-height: 1;
        display: inline-block;
        i {
          font-size: 14px;
          margin-right: 2px;
        }
        &:hover {
          color: @xtxColor;
        }
      }

      .logout-btn {
        padding: 0 15px;
        color: #cdcdcd;
        line-height: 1;
        display: inline-block;
        border: none;
        background: transparent;
        cursor: pointer;

        &:hover {
          color: @xtxColor;
        }
      }

      .top-avatar {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        vertical-align: middle;
        margin-right: 4px;
        object-fit: cover;
      }

      ~ li {
        a,
        .logout-btn {
          border-left: 2px solid #666;
        }
      }
    }
  }
}
</style>
