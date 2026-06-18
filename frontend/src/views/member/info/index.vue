<template>
  <div class="member-info">
    <div class="member-nav">
      <span class="title">个人信息</span>
    </div>
    <div class="info-form" v-if="profile">
      <div class="form-item">
        <div class="label">头像：</div>
        <div class="field">
          <img :src="pendingAvatar || profile.avatar" class="avatar" />
          <button class="change-avatar-btn" @click="$refs.avatarInput.click()">更换头像</button>
          <input ref="avatarInput" type="file" accept="image/jpeg,image/jpg,image/png,image/webp" class="hidden-input" @change="onAvatarFileSelect" />
        </div>
      </div>
      <div class="form-item">
        <div class="label">账号：</div>
        <div class="field">{{ profile.account }}</div>
      </div>
      <div class="form-item">
        <div class="label">手机号：</div>
        <div class="field">{{ profile.mobile }}</div>
      </div>
      <div class="form-item">
        <div class="label">昵称：</div>
        <div class="field">
          <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
        </div>
      </div>
      <div class="form-item">
        <div class="label">性别：</div>
        <div class="field">
          <label class="radio">
            <input type="radio" v-model="form.gender" :value="0" /> 男
          </label>
          <label class="radio">
            <input type="radio" v-model="form.gender" :value="1" /> 女
          </label>
          <label class="radio">
            <input type="radio" v-model="form.gender" :value="null" /> 保密
          </label>
        </div>
      </div>
      <div class="form-item">
        <div class="label">生日：</div>
        <div class="field">
          <input v-model="form.birthday" type="date" class="input" />
        </div>
      </div>
      <div class="form-item">
        <div class="label">职业：</div>
        <div class="field">
          <input v-model="form.profession" class="input" placeholder="请输入职业" />
        </div>
      </div>
      <div class="form-item">
        <div class="label"></div>
        <div class="field">
          <XtxButton @click="save()" type="primary">保存</XtxButton>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>

    <!-- Avatar cropper modal -->
    <AvatarCropper v-if="cropperVisible" :imageSource="cropperImage"
      @confirm="onCropperConfirm" @cancel="onCropperCancel" />
  </div>
</template>

<script>
import { reactive, ref, onMounted } from "vue";
import { findUserProfile, updateUserProfile } from "@/api/member";
import { useStore } from "vuex";
import Message from "@/components/library/Message";
import AvatarCropper from "./components/avatar-cropper.vue";

export default {
  name: "MemberInfo",
  components: { AvatarCropper },
  setup() {
    const store = useStore();
    const profile = ref(null);
    const pendingAvatar = ref(null);   // cropped but not yet saved
    const cropperVisible = ref(false);
    const cropperImage = ref("");       // source dataURL for cropper
    const form = reactive({
      nickname: "",
      gender: null,
      birthday: "",
      profession: "",
    });

    onMounted(async () => {
      try {
        const data = await findUserProfile();
        profile.value = data.result;
        // 同步完整 profile 到 store，但保留现有字段（如 token）
        const existingProfile = store.state.user.profile;
        store.commit("user/setUser", { ...existingProfile, ...data.result });
        form.nickname = data.result.nickname || "";
        form.gender = data.result.gender !== undefined ? data.result.gender : null;
        form.birthday = data.result.birthday || "";
        form.profession = data.result.profession || "";
      } catch (e) {
        Message({ type: "error", text: "加载用户信息失败" });
      }
    });

    const save = async () => {
      try {
        const data = await updateUserProfile({
          nickname: form.nickname,
          gender: form.gender,
          birthday: form.birthday,
          profession: form.profession,
          ...(pendingAvatar.value ? { avatar: pendingAvatar.value } : {}),
        });
        profile.value = data.result;
        // 同步完整 profile 到 store，但保留现有字段（如 token）
        const existingProfile = store.state.user.profile;
        store.commit("user/setUser", { ...existingProfile, ...data.result });
        // 保存成功后清除 pendingAvatar（已同步到后端返回的 profile）
        pendingAvatar.value = null;
        Message({ type: "success", text: "保存成功" });
      } catch (e) {
        Message({ type: "error", text: "保存失败" });
      }
    };

    // Step 1: User selects a file → validate → open cropper
    const onAvatarFileSelect = (e) => {
      const file = e.target.files?.[0];
      // 重置 input 以便重复选择同一文件
      e.target.value = "";
      if (!file) return;

      // 验证文件类型
      const validTypes = ["image/jpeg", "image/jpg", "image/png", "image/webp"];
      if (!validTypes.includes(file.type)) {
        Message({ type: "warn", text: "请选择图片文件" });
        return;
      }
      // 文件大小限制 1MB
      if (file.size > 1 * 1024 * 1024) {
        Message({ type: "warn", text: "图片大小不能超过 1MB" });
        return;
      }

      const reader = new FileReader();
      reader.onload = (ev) => {
        cropperImage.value = ev.target.result;
        cropperVisible.value = true;
      };
      reader.onerror = () => {
        Message({ type: "error", text: "图片读取失败，请重新选择" });
      };
      reader.readAsDataURL(file);
    };

    // Step 2: Cropper confirmed → update preview only (not saved yet)
    const onCropperConfirm = (croppedDataURL) => {
      pendingAvatar.value = croppedDataURL;
      cropperVisible.value = false;
      cropperImage.value = "";
    };

    // Step 2b: Cropper cancelled
    const onCropperCancel = () => {
      cropperVisible.value = false;
      cropperImage.value = "";
    };

    return {
      profile, pendingAvatar, form, save,
      cropperVisible, cropperImage,
      onAvatarFileSelect, onCropperConfirm, onCropperCancel,
    };
  },
};
</script>

<style scoped lang="less">
.member-info {
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
  .info-form {
    padding: 30px 40px;
    .form-item {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      .label {
        width: 80px;
        text-align: right;
        font-size: 14px;
        color: #666;
        margin-right: 15px;
      }
      .field {
        flex: 1;
        font-size: 14px;
        .avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
        }
        .change-avatar-btn {
          margin-left: 12px;
          padding: 6px 16px;
          border: 1px solid @xtxColor;
          border-radius: 4px;
          background: #fff;
          color: @xtxColor;
          cursor: pointer;
          font-size: 13px;
          vertical-align: middle;
          &:hover {
            background: @xtxColor;
            color: #fff;
          }
        }
        .hidden-input {
          display: none;
        }
        .input {
          width: 320px;
          height: 36px;
          border: 1px solid #cfcdcd;
          padding: 0 10px;
          outline: none;
          &:focus {
            border-color: @xtxColor;
          }
        }
        .radio {
          margin-right: 20px;
          input {
            margin-right: 4px;
          }
        }
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
</style>
