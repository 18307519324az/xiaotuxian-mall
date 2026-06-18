const path = require("path");
const webpack = require("webpack");

module.exports = {
  // 启用并行构建加速
  parallel: true,
  // 让 babel 转译这些使用了现代语法的 node_modules 包（webpack 4 的 acorn 无法解析）
  transpileDependencies: [/@vue\/devtools.*/, /birpc/],
  pluginOptions: {
    "style-resources-loader": {
      preProcessor: "less",
      // 哪些文件自动引入，使用绝对路径
      // 需要是path.join来拼接完整路径
      patterns: [
        path.join(__dirname, "./src/assets/styles/variables.less"),
        path.join(__dirname, "./src/assets/styles/mixins.less"),
      ],
    },
  },
  chainWebpack: (config) => {
    // 图片加载
    config.module
      .rule("images")
      .use("url-loader")
      .loader("url-loader")
      .tap((options) => Object.assign(options, { limit: 10000 }));

    // 开启IP域名访问
    config.devServer.disableHostCheck(true);

    // 禁止 webpack-dev-server 客户端错误覆盖层（干扰 Playwright 测试交互）
    config.devServer.set("overlay", false);

    // 禁止浏览器缓存 HTML 入口文件，确保用户每次加载最新 JS bundle
    config.devServer.set("headers", {
      "Cache-Control": "no-store, no-cache, must-revalidate, proxy-revalidate",
      "Pragma": "no-cache",
      "Expires": "0",
    });

    // 代理 API 请求到本地微服务网关，解决开发环境跨域问题
    const addressTarget = process.env.VUE_APP_ADDRESS_SERVICE_MODE === "real"
      ? "http://localhost:8110"
      : "http://localhost:8099";
    const categoryTarget = process.env.VUE_APP_CATEGORY_SERVICE_MODE === "real"
      ? "http://localhost:8104"
      : "http://localhost:8099";
    const goodsTarget = process.env.VUE_APP_GOODS_SERVICE_MODE === "real"
      ? "http://localhost:8105"
      : "http://localhost:8099";
    const brandTarget = process.env.VUE_APP_BRAND_SERVICE_MODE === "real"
      ? "http://localhost:8105"
      : "http://localhost:8099";
    const topicTarget = process.env.VUE_APP_TOPIC_SERVICE_MODE === "real"
      ? "http://localhost:8105"
      : "http://localhost:8099";
    const goodsListTarget = process.env.VUE_APP_GOODS_LIST_SERVICE_MODE === "real"
      ? "http://localhost:8105"
      : "http://localhost:8099";
    config.devServer.proxy({
      // 地址服务专用路由（支持环境变量切换 mock/real）
      "/api/member/address": {
        target: addressTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 商品列表/搜索服务专用路由（v1.6，支持环境变量切换 mock/real）
      // 必须在 /api/category 之前，防止被分类规则拦截
      "/api/category/goods/temporary": {
        target: goodsListTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 分类服务专用路由（支持环境变量切换 mock/real），必须在 /api 通用路由之前
      "/api/home/category/head": {
        target: categoryTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      "/api/category": {
        target: categoryTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 商品详情相关路由（支持环境变量切换 mock/real）
      // 非详情接口必须优先匹配，避免被 /api/goods 通用规则拦截
      "/api/goods/relevant": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      "/api/goods/hot": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      "/api/goods/sku": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      "/api/goods/stock": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 商品评价相关接口必须始终走 Mock 聚合服务（8099），因为 MockController 做
      // runtimeGoodsReviews 合并。不能被 /api/goods 通用规则劫持到 goods-service（8105）。
      // 使用尾随斜杠 /api/goods/ 避免匹配 /api/goods?id=X（详情走 query 参数）。
      "/api/goods/": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      "/api/goods": {
        target: goodsTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 品牌详情相关路由（支持环境变量切换 mock/real）
      // 必须在 /api 通用路由之前，避免被兜底拦截
      // /api/home/brand 和 /api/member/brand-follow 不受影响（不走此规则）
      "/api/brand": {
        target: brandTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 专题详情相关路由（支持环境变量切换 mock/real）
      // 使用 /api/topic/（尾随斜杠）避免拦截 /api/topic 列表接口
      "/api/topic/": {
        target: topicTarget,
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
      // 其他所有 API 请求走 Mock 服务
      "/api": {
        target: "http://localhost:8099",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
    });

    // 让 .mjs 文件也走 babel 转译（webpack 4 默认不处理 .mjs）
    config.module
      .rule("mjs")
      .test(/\.mjs$/)
      .type("javascript/auto")
      .use("babel-loader")
      .loader("babel-loader");
  },
  configureWebpack: {
    externals: {
      qc: "QC",
    },
    plugins: [
      // 定义 Vue 3 特性标志（消除控制台警告）
      new webpack.DefinePlugin({
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: "false",
        __VUE_OPTIONS_API__: "true",
        __VUE_PROD_DEVTOOLS__: "false",
      }),
    ],
  },
};
