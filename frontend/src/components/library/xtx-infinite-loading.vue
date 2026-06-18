<template>
  <div class="xtx-infinite-loading" ref="target">
    <div class="loading" v-if="loading">
      <span class="img"></span>
      <span class="text">正在加载...</span>
    </div>
    <div class="none" v-if="finished">
      <span class="img"></span>
      <span class="text">亲，没有更多了</span>
    </div>
  </div>
</template>
<script>
import { ref, watch, nextTick } from "vue";
import { useIntersectionObserver } from "@vueuse/core";
export default {
  name: "XtxInfiniteLoading",
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
    finished: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const target = ref(null);
    // 监听target是否进入可视区
    useIntersectionObserver(
      target,
      ([{ isIntersecting }]) => {
        if (isIntersecting) {
          // 触发加载事件条件：请求加载完成，数据加载完毕
          if (!props.loading && !props.finished) {
            emit("infinite");
          }
        }
      },
      {
        threshold: 0,
      }
    );

    // 当 finished 从 true → false（父组件切换分类并清空数据时），
    // target 元素可能已经处于可视区（清空列表后页面变短），
    // 但 useIntersectionObserver 对于已经相交的元素不会重复触发回调。
    // 此时需要手动创建一个一次性观察器来检测 target 是否在可视区。
    watch(() => props.finished, (newVal, oldVal) => {
      if (oldVal === true && newVal === false && target.value) {
        nextTick(() => {
          const observer = new IntersectionObserver(([entry]) => {
            if (entry.isIntersecting && !props.loading && !props.finished) {
              emit("infinite");
            }
            observer.disconnect();
          }, { threshold: 0 });
          observer.observe(target.value);
        });
      }
    });

    return { target };
  },
};
</script>
<style scoped lang="less">
.xtx-infinite-loading {
  .loading {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    .img {
      width: 50px;
      height: 50px;
      background: url(../../assets/images/load.gif) no-repeat center / contain;
    }
    .text {
      color: #999;
      font-size: 16px;
    }
  }
  .none {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    .img {
      width: 200px;
      height: 134px;
      background: url(../../assets/images/none.png) no-repeat center / contain;
    }
    .text {
      color: #999;
      font-size: 16px;
    }
  }
}
</style>
