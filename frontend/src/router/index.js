import { createRouter, createWebHashHistory } from "vue-router";
import store from "@/store";
import { h } from "vue";
const Layout = () => import("@/views/Layout");
const Home = () => import("@/views/home/index");
import TopCategory from "@/views/category";
import SubCategory from "@/views/category/sub";
const Goods = () => import("@/views/goods/index");
const Similar = () => import("@/views/similar/index");
const Brand = () => import("@/views/brand/index");
const Special = () => import("@/views/special/index");
const Login = () => import("@/views/login/index");
const LoginCallback = () => import("@/views/login/callback");
const Register = () => import("@/views/login/register");
const Cart = () => import("@/views/cart/index");
const PayCheckout = () => import("@/views/member/pay/checkout");
const PayIndex = () => import("@/views/member/pay/index");
const MemberLayout = () => import("@/views/member/Layout");
const MemberHome = () => import("@/views/member/home");
const MemberOrder = () => import("@/views/member/order");
const MemberOrderDetail = () => import("@/views/member/order/detail");
const MemberInfo = () => import("@/views/member/info/index");
const MemberSecurity = () => import("@/views/member/security/index");
const MemberCollect = () => import("@/views/member/collect/index");
const MemberHistory = () => import("@/views/member/history/index");
const MemberAddress = () => import("@/views/member/address/index");
const MemberMessage = () => import("@/views/member/message/index");
const MemberCoupon = () => import("@/views/member/coupon/index");
const MemberPoints = () => import("@/views/member/points/index");
const MemberGiftCard = () => import("@/views/member/gift-card/index");
const MemberReview = () => import("@/views/member/review/index");
const MemberAfterSale = () => import("@/views/member/after-sale/index");
const MemberInvite = () => import("@/views/member/invite/index");
const MemberLottery = () => import("@/views/member/lottery/index");
const MemberTopicCollect = () => import("@/views/member/topic-collect/index");
const MemberBrandFollow = () => import("@/views/member/brand-follow/index");
const MemberCustomerService = () => import("@/views/member/customer-service/index");
const SearchPage = () => import("@/views/search/index");
const HelpPage = () => import("@/views/help/index");
const AboutPage = () => import("@/views/about/index");
const MobilePage = () => import("@/views/mobile/index");

const routes = [
  {
    path: "/",
    component: Layout,
    children: [
      { path: "/", component: Home },
      { path: "/category/:id", component: TopCategory },
      { path: "/category/sub/:id", component: SubCategory },
      { path: "/product/:id", component: Goods },
      { path: "/similar/:id", component: Similar },
      { path: "/brand/:id", component: Brand },
      { path: "/brand", component: Brand },
      { path: "/special/:id", component: Special },
      { path: "/special", component: Special },
      { path: "/help", component: HelpPage },
      { path: "/about", component: AboutPage },
      { path: "/mobile", component: MobilePage },
      { path: "/search", component: SearchPage },
      { path: "/cart", component: Cart },
      { path: "/member/checkout", component: PayCheckout },
      { path: "/member/pay", component: PayIndex },
      {
        path: "/member",
        component: MemberLayout,
        children: [
          { path: "/member", component: MemberHome },
          {
            path: "/member/order",
            // 创建一个RouterView容器形成嵌套关系
            component: { render: () => h(<RouterView />) },
            children: [
              { path: "", component: MemberOrder },
              { path: ":id", component: MemberOrderDetail },
            ],
          },
          { path: "/member/info", component: MemberInfo },
          { path: "/member/security", component: MemberSecurity },
          { path: "/member/collect", component: MemberCollect },
          { path: "/member/history", component: MemberHistory },
          { path: "/member/address", component: MemberAddress },
          { path: "/member/message", component: MemberMessage },
          { path: "/member/coupon", component: MemberCoupon },
          { path: "/member/points", component: MemberPoints },
          { path: "/member/gift-card", component: MemberGiftCard },
          { path: "/member/review", component: MemberReview },
          { path: "/member/after-sale", component: MemberAfterSale },
          { path: "/member/invite", component: MemberInvite },
          { path: "/member/lottery", component: MemberLottery },
          { path: "/member/topic-collect", component: MemberTopicCollect },
          { path: "/member/brand-follow", component: MemberBrandFollow },
          { path: "/member/customer-service", component: MemberCustomerService },
        ],
      },
    ],
  },
  { path: "/register", component: Register },
  { path: "/login", component: Login },
  { path: "/login/callback", component: LoginCallback },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { left: 0, top: 0 };
  },
});

// 路由守卫：未登录访问 /member/** 重定向到登录页
router.beforeEach((to, from, next) => {
  if (to.path.startsWith("/member")) {
    const token = store.state.user?.profile?.token;
    if (!token) {
      next("/login?redirectUrl=" + encodeURIComponent(to.fullPath));
      return;
    }
  }
  next();
});

export default router;
