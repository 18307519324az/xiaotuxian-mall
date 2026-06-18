<template>
  <LoginHeader>欢迎注册</LoginHeader>
  <section class="register-section">
    <div class="wrapper">
      <div class="title">
        <h3>用户注册</h3>
        <p>已有账号？<RouterLink to="/login">去登录</RouterLink></p>
      </div>
      <Form ref="formCom" class="form" :validation-schema="schema" v-slot="{ errors }" autocomplete="off">
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-user"></i>
            <Field
              :class="{ error: errors.account }"
              v-model="form.account"
              name="account"
              type="text"
              placeholder="请输入用户名"
            />
          </div>
          <div class="error" v-if="errors.account">
            <i class="iconfont icon-warning" />{{ errors.account }}
          </div>
        </div>
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-lock"></i>
            <Field
              :class="{ error: errors.password }"
              v-model="form.password"
              name="password"
              type="password"
              placeholder="请输入密码"
            />
          </div>
          <div class="error" v-if="errors.password">
            <i class="iconfont icon-warning" />{{ errors.password }}
          </div>
        </div>
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-lock"></i>
            <Field
              :class="{ error: errors.rePassword }"
              v-model="form.rePassword"
              name="rePassword"
              type="password"
              placeholder="请确认密码"
            />
          </div>
          <div class="error" v-if="errors.rePassword">
            <i class="iconfont icon-warning" />{{ errors.rePassword }}
          </div>
        </div>
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-user"></i>
            <Field
              :class="{ error: errors.nickname }"
              v-model="form.nickname"
              name="nickname"
              type="text"
              placeholder="请输入昵称（可选）"
            />
          </div>
          <div class="error" v-if="errors.nickname">
            <i class="iconfont icon-warning" />{{ errors.nickname }}
          </div>
        </div>
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-phone"></i>
            <Field
              :class="{ error: errors.mobile }"
              v-model="form.mobile"
              name="mobile"
              type="text"
              placeholder="请输入手机号（可选）"
            />
          </div>
          <div class="error" v-if="errors.mobile">
            <i class="iconfont icon-warning" />{{ errors.mobile }}
          </div>
        </div>
        <div class="form-item">
          <div class="input">
            <i class="iconfont icon-user"></i>
            <Field
              :class="{ error: errors.inviteCode }"
              v-model="form.inviteCode"
              name="inviteCode"
              type="text"
              placeholder="邀请码（可选）"
            />
          </div>
          <div class="error" v-if="errors.inviteCode">
            <i class="iconfont icon-warning" />{{ errors.inviteCode }}
          </div>
        </div>
        <div class="form-item">
          <div class="agree">
            <Field as="XtxCheckbox" name="isAgree" v-model="form.isAgree" />
            <span>我已同意</span>
            <a href="javascript:;">《隐私条款》</a>
            <span>和</span>
            <a href="javascript:;">《服务条款》</a>
          </div>
          <div class="error" v-if="errors.isAgree">
            <i class="iconfont icon-warning" />{{ errors.isAgree }}
          </div>
        </div>
        <a @click="register()" href="javascript:;" class="btn">注册</a>
      </Form>
    </div>
  </section>
  <LoginFooter />
</template>

<script>
import { reactive, ref } from "vue";
import { Form, Field } from "vee-validate";
import schema from "@/utils/vee-validate-schema";
import Message from "@/components/library/Message";
import LoginHeader from "./components/login-header";
import LoginFooter from "./components/login-footer";
import { userRegister } from "@/api/member";
import { useStore } from "vuex";
import { useRouter, useRoute } from "vue-router";

export default {
  name: "Register",
  components: { LoginHeader, LoginFooter, Form, Field },
  setup() {
    const form = reactive({
      account: "",
      password: "",
      rePassword: "",
      nickname: "",
      mobile: "",
      inviteCode: "",
      isAgree: true,
    });

    const mySchema = {
      account: schema.account,
      password: schema.password,
      rePassword: schema.rePassword,
      nickname(value) {
        if (value && value.length > 20) return "昵称不超过20个字符";
        return true;
      },
      mobile(value) {
        if (!value) return true; // optional
        if (!/^1[3-9]\d{9}$/.test(value)) return "手机号格式不对";
        return true;
      },
      inviteCode(value) {
        if (!value) return true; // optional
        if (value.length < 5) return "邀请码格式不正确";
        return true;
      },
      isAgree: schema.isAgree,
    };

    const formCom = ref(null);
    const store = useStore();
    const router = useRouter();
    const route = useRoute();

    // 自动填充邀请码（从分享链接跳转过来时）
    if (route.query.inviteCode) {
      form.inviteCode = route.query.inviteCode;
    }

    const register = async () => {
      const valid = await formCom.value.validate();
      if (!valid) return;
      try {
        const data = await userRegister({
          account: form.account,
          password: form.password,
          nickname: form.nickname || form.account,
          mobile: form.mobile || "",
          inviteCode: form.inviteCode || "",
        });
        if (!data || data.code !== "20000") {
          throw { response: { data: { message: data?.msg || "注册失败" } } };
        }
        const { id, account, avatar, mobile, nickname, token } = data.result;
        store.commit("user/setUser", { id, account, avatar, mobile, nickname, token });
        Message({ type: "success", text: "注册成功" });
        router.push("/");
      } catch (e) {
        if (e.response && e.response.data) {
          Message({ type: "error", text: e.response.data.message || "注册失败" });
        } else {
          Message({ type: "error", text: "网络请求失败，请稍后重试" });
        }
      }
    };

    return { form, schema: mySchema, formCom, register };
  },
};
</script>

<style scoped lang="less">
.register-section {
  background: url(../../assets/images/login-bg.png) no-repeat center / cover;
  height: 600px;
  position: relative;
  .wrapper {
    width: 380px;
    background: #fff;
    min-height: 500px;
    position: absolute;
    left: 50%;
    top: 54px;
    transform: translate3d(100px, 0, 0);
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.15);
    padding: 0 40px;
    padding-top: 30px;
    .title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      h3 {
        font-size: 22px;
        font-weight: normal;
      }
      p {
        font-size: 14px;
        color: #999;
        a {
          color: @xtxColor;
        }
      }
    }
    .form {
      &-item {
        margin-bottom: 28px;
        .input {
          position: relative;
          height: 36px;
          > i {
            width: 34px;
            height: 34px;
            background: #cfcdcd;
            color: #fff;
            position: absolute;
            left: 1px;
            top: 1px;
            text-align: center;
            line-height: 34px;
            font-size: 18px;
          }
          input {
            padding-left: 44px;
            border: 1px solid #cfcdcd;
            height: 36px;
            line-height: 36px;
            width: 100%;
            &.error {
              border-color: @priceColor;
            }
            &:focus {
              border-color: @xtxColor;
            }
          }
        }
        > .error {
          font-size: 12px;
          line-height: 28px;
          color: @priceColor;
          i {
            font-size: 14px;
            margin-right: 2px;
          }
        }
      }
      .agree {
        a {
          color: #069;
        }
      }
      .btn {
        display: block;
        width: 100%;
        height: 40px;
        color: #fff;
        text-align: center;
        line-height: 40px;
        background: @xtxColor;
        cursor: pointer;
      }
    }
  }
}
</style>
