import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "normalize.css";
import "@/assets/styles/common.less";
import ui from "./components/library";
import "./mock";
import placeholderImg from "@/assets/images/none.png";

const app = createApp(App);

// 全局指令：图片加载失败时替换为本地兜底图
app.directive("img-error", {
  mounted(el) {
    const handler = function () {
      el.src = placeholderImg;
      el.removeEventListener("error", handler);
    };
    el.addEventListener("error", handler);
  },
});

app.use(store).use(router).use(ui).mount("#app");
