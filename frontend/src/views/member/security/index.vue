<template>
  <div class="member-security">
    <div class="member-nav">
      <span class="title">账户安全</span>
    </div>
    <div class="security-list" v-if="profile">
      <div class="security-item">
        <div class="info">
          <span class="iconfont icon-phone"></span>
          <div>
            <p class="label">手机号</p>
            <p class="value">{{ maskedMobile }}</p>
          </div>
        </div>
        <div class="action">
          <span v-if="profile.mobile" class="status bound">已绑定</span>
          <span v-else class="status unbound">未绑定</span>
          <XtxButton v-if="profile.mobile" @click="showBindMobile = true" type="plain" size="small" style="margin-left:10px">修改手机号</XtxButton>
          <XtxButton v-else @click="showBindMobile = true" type="plain" size="small" style="margin-left:10px">绑定手机号</XtxButton>
        </div>
      </div>
      <div class="security-item">
        <div class="info">
          <span class="iconfont icon-lock"></span>
          <div>
            <p class="label">登录密码</p>
            <p class="value">已设置</p>
          </div>
        </div>
        <div class="action">
          <XtxButton @click="showChangePwd = true" type="plain" size="small">修改密码</XtxButton>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>

    <!-- 修改密码对话框 -->
    <XtxDialog title="修改密码" v-model:visible="showChangePwd">
      <div class="change-pwd-form">
        <div class="field">
          <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" class="input" />
        </div>
        <div class="field">
          <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（6-24个字符）" class="input" />
        </div>
        <div class="field">
          <input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" class="input" />
        </div>
        <div class="error" v-if="pwdError">{{ pwdError }}</div>
      </div>
      <template #footer>
        <XtxButton @click="showChangePwd = false" type="gray" style="margin-right: 20px">取消</XtxButton>
        <XtxButton @click="submitChangePwd()" type="primary">确认</XtxButton>
      </template>
    </XtxDialog>

    <!-- 绑定/修改手机号对话框 -->
    <XtxDialog title="绑定手机号" v-model:visible="showBindMobile">
      <div class="bind-mobile-form">
        <div class="field">
          <input v-model="bindMobileForm.mobile" type="text" placeholder="请输入11位手机号" class="input" maxlength="11" />
        </div>
        <div class="error" v-if="bindMobileError">{{ bindMobileError }}</div>
      </div>
      <template #footer>
        <XtxButton @click="showBindMobile = false" type="gray" style="margin-right: 20px">取消</XtxButton>
        <XtxButton @click="submitBindMobile()" type="primary">确认</XtxButton>
      </template>
    </XtxDialog>
  </div>
</template>

<script>
import { ref, computed, reactive, onMounted } from "vue";
import { findUserProfile, changePassword, bindMobile } from "@/api/member";
import { useStore } from "vuex";
import Message from "@/components/library/Message";

export default {
  name: "MemberSecurity",
  setup() {
    const store = useStore();
    const profile = ref(null);
    const showChangePwd = ref(false);
    const pwdError = ref("");
    const showBindMobile = ref(false);
    const bindMobileForm = reactive({ mobile: "" });
    const bindMobileError = ref("");

    const pwdForm = reactive({
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    });

    onMounted(async () => {
      try {
        const data = await findUserProfile();
        profile.value = data.result;
      } catch (e) {
        // ignore
      }
    });

    const maskedMobile = computed(() => {
      const mobile = profile.value?.mobile || store.state.user.profile.mobile;
      if (!mobile) return "未绑定";
      return mobile.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
    });

    const submitChangePwd = async () => {
      pwdError.value = "";
      if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
        pwdError.value = "请填写完整的密码信息";
        return;
      }
      if (pwdForm.newPassword.length < 6 || pwdForm.newPassword.length > 24) {
        pwdError.value = "新密码需6-24个字符";
        return;
      }
      if (pwdForm.newPassword !== pwdForm.confirmPassword) {
        pwdError.value = "两次输入的新密码不一致";
        return;
      }
      try {
        await changePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword,
        });
        Message({ type: "success", text: "修改密码成功" });
        showChangePwd.value = false;
        pwdForm.oldPassword = "";
        pwdForm.newPassword = "";
        pwdForm.confirmPassword = "";
      } catch (e) {
        if (e.response && e.response.data) {
          pwdError.value = e.response.data.message || "修改密码失败";
        } else {
          pwdError.value = "网络请求失败";
        }
      }
    };

    const submitBindMobile = async () => {
      bindMobileError.value = "";
      if (!bindMobileForm.mobile || !/^1[3-9]\d{9}$/.test(bindMobileForm.mobile)) {
        bindMobileError.value = "请输入正确的11位手机号";
        return;
      }
      try {
        const data = await bindMobile(bindMobileForm.mobile);
        // 更新本地 profile
        if (profile.value) {
          profile.value.mobile = bindMobileForm.mobile;
        }
        store.commit("user/setUser", { ...store.state.user.profile, mobile: bindMobileForm.mobile });
        Message({ type: "success", text: "手机号绑定成功" });
        showBindMobile.value = false;
        bindMobileForm.mobile = "";
      } catch (e) {
        if (e.response && e.response.data) {
          bindMobileError.value = e.response.data.message || "绑定失败";
        } else {
          bindMobileError.value = "网络请求失败";
        }
      }
    };

    return { profile, maskedMobile, showChangePwd, pwdForm, pwdError, submitChangePwd, showBindMobile, bindMobileForm, bindMobileError, submitBindMobile };
  },
};
</script>

<style scoped lang="less">
.member-security {
  background: #fff;
  min-height: 400px;
  .member-nav {
    padding: 20px;
    border-bottom: 1px solid #f5f5f5;
    .title {
      font-size: 18px;
      font-weight: 500;
    }
  }
  .security-list {
    padding: 20px 40px;
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid #f5f5f5;
      .info {
        display: flex;
        align-items: center;
        .iconfont {
          font-size: 32px;
          color: @xtxColor;
          margin-right: 15px;
        }
        .label {
          font-size: 16px;
          font-weight: 500;
        }
        .value {
          font-size: 14px;
          color: #999;
          margin-top: 4px;
        }
      }
      .status.bound {
        color: #52c41a;
        font-size: 14px;
      }
      .status.unbound {
        color: #ff4d4f;
        font-size: 14px;
      }
    }
  }
  .loading {
    text-align: center;
    padding: 100px 0;
    color: #999;
    font-size: 14px;
  }
}
.change-pwd-form {
  padding: 10px 0;
  .field {
    margin-bottom: 15px;
    .input {
      width: 100%;
      height: 40px;
      border: 1px solid #cfcdcd;
      padding: 0 10px;
      outline: none;
      &:focus {
        border-color: @xtxColor;
      }
    }
  }
  .error {
    color: @priceColor;
    font-size: 12px;
    margin-top: -5px;
  }
}
.bind-mobile-form {
  padding: 10px 0;
  .field {
    margin-bottom: 15px;
    .input {
      width: 100%;
      height: 40px;
      border: 1px solid #cfcdcd;
      padding: 0 10px;
      outline: none;
      &:focus {
        border-color: @xtxColor;
      }
    }
  }
  .error {
    color: @priceColor;
    font-size: 12px;
    margin-top: -5px;
  }
}
</style>
