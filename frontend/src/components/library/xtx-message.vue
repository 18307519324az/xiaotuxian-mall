<template>
  <Transition name="down">
    <div class="xtx-message" :style="style[type]" v-show="visible">
      <i class="iconfont" :class="[style[type].icon]"></i>
      <span class="text">{{ text }}</span>
    </div>
  </Transition>
</template>
<script>
import { onMounted, ref } from "vue";
export default {
  name: "XtxMessage",
  props: {
    type: {
      type: String,
      default: "warn",
    },
    text: {
      type: String,
      default: "",
    },
  },
  setup() {
    const style = {
      warn: {
        icon: "icon-warning",
        color: "#E6A23C",
        backgroundColor: "rgb(253, 246, 236)",
        borderColor: "rgb(250, 236, 216)",
      },
      error: {
        icon: "icon-shanchu",
        color: "#F56C6C",
        backgroundColor: "rgb(254, 240, 240)",
        borderColor: "rgb(253, 226, 226)",
      },
      success: {
        icon: "icon-queren2",
        color: "#67C23A",
        backgroundColor: "rgb(240, 249, 235)",
        borderColor: "rgb(225, 243, 216)",
      },
    };
    const visible = ref(false);
    onMounted(() => {
      visible.value = true;
    });
    return { style, visible };
  },
};
</script>
<style scoped lang="less">
.down {
  &-enter {
    &-from {
      transform: translate3d(-50%, -75px, 0);
      opacity: 0;
    }
    &-active {
      transition: all 0.5s;
    }
    &-to {
      transform: translateX(-50%);
      opacity: 1;
    }
  }
}
.xtx-message {
  width: min(520px, calc(100vw - 32px));
  min-height: 56px;
  position: fixed;
  z-index: 9999;
  left: 50%;
  transform: translateX(-50%);
  top: 72px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border: 1px solid #e4e4e4;
  background: #f5f5f5;
  color: #999;
  border-radius: 8px;
  box-sizing: border-box;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  i {
    flex-shrink: 0;
  }
  .text {
    line-height: 1.6;
    word-break: break-all;
  }
}
</style>
