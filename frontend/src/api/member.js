import request from "@/utils/request";

/**
 * 获取收藏分页数据
 * @param {Integer} collectType - 收藏类型，1为商品，2为专题，3为品牌
 * @returns
 */
export const findCollect = ({ page = 1, pageSize = 10, collectType = 1 }) => {
  return request("/member/collect", "get", { page, pageSize, collectType });
};

/**
 * 获取用户资料
 * @returns
 */
export const findUserProfile = () => {
  return request("/member/profile", "get");
};

/**
 * 修改用户资料
 * @param {Object} data - { nickname, gender, birthday, profession }
 * @returns
 */
export const updateUserProfile = (data) => {
  return request("/member/profile", "put", data);
};

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 * @returns
 */
export const changePassword = (data) => {
  return request("/member/password", "put", data);
};

/**
 * 取消收藏
 * @param {String} goodsId - 商品ID
 * @returns
 */
export const deleteCollect = (goodsId) => {
  return request("/member/collect", "delete", { goodsId });
};

/**
 * 获取足迹分页数据
 * @param {Object} params - { page, pageSize }
 * @returns
 */
export const findHistory = ({ page = 1, pageSize = 10 }) => {
  return request("/member/history", "get", { page, pageSize });
};

/**
 * 添加足迹
 * @param {String} goodsId - 商品ID
 * @returns
 */
export const addHistory = (goodsId) => {
  return request("/member/history", "post", { goodsId });
};

/**
 * 清空足迹
 * @returns
 */
export const clearHistory = () => {
  return request("/member/history", "delete");
};

/**
 * 用户注册
 * @param {Object} data - { account, password, mobile, nickname }
 * @returns
 */
export const findMemberMessages = () => {
  return request("/member/message", "get");
};

export const readMemberMessage = (payload) => {
  return request("/member/message/read", "post", payload);
};

export const findMemberCoupons = () => {
  return request("/member/coupon", "get");
};

export const exchangeMemberCoupon = (code) => {
  return request("/member/coupon/exchange", "post", { code });
};

export const findMemberPoints = () => {
  return request("/member/points", "get");
};

export const findGiftCards = () => {
  return request("/member/gift-card", "get");
};

export const bindGiftCard = (code) => {
  return request("/member/gift-card/bind", "post", { code });
};

export const findMemberReviews = (params = {}) => {
  return request("/member/review", "get", params);
};

export const submitMemberReview = (data) => {
  return request("/member/review", "post", data);
};

export const findAfterSales = (params = {}) => {
  return request("/member/after-sale", "get", params);
};

export const submitAfterSale = (data) => {
  return request("/member/after-sale", "post", data);
};

export const findTopicCollect = ({ page = 1, pageSize = 10 } = {}) => {
  return request("/member/topic-collect", "get", { page, pageSize });
};

export const addTopicCollect = (id) => {
  return request(`/member/topic-collect/${id}`, "post");
};

export const deleteTopicCollect = (id) => {
  return request(`/member/topic-collect/${id}`, "delete");
};

export const findBrandFollow = ({ page = 1, pageSize = 10 } = {}) => {
  return request("/member/brand-follow", "get", { page, pageSize });
};

export const addBrandFollow = (id) => {
  return request(`/member/brand-follow/${id}`, "post");
};

export const deleteBrandFollow = (id) => {
  return request(`/member/brand-follow/${id}`, "delete");
};

export const findInviteInfo = () => {
  return request("/member/invite", "get");
};

export const findLotteryInfo = () => {
  return request("/member/lottery", "get");
};

export const drawLottery = () => {
  return request("/member/lottery/draw", "post");
};

export const userRegister = (data) => {
  return request("/member/register", "post", data);
};

/**
 * 退出登录
 * @returns
 */
export const userLogout = () => {
  return request("/member/logout", "post");
};

/**
 * 绑定手机号
 * @param {String} mobile - 手机号
 * @returns
 */
export const bindMobile = (mobile) => {
  return request("/member/mobile", "put", { mobile });
};

/**
 * 客服智能问答
 * @param {String} message - 用户消息
 * @returns
 */
export const customerChat = (message) => {
  return request("/member/customer-service/chat", "post", { message });
};

/**
 * 获取客服聊天记录
 * @returns
 */
export const getCustomerChatHistory = () => {
  return request("/member/customer-service/history", "get");
};

/**
 * 提交客服工单
 * @param {Object} data - { type, content, orderId }
 * @returns
 */
export const createCustomerTicket = (data) => {
  return request("/member/customer-service/ticket", "post", data);
};

/**
 * 获取客服工单列表
 * @returns
 */
export const getCustomerTickets = () => {
  return request("/member/customer-service/ticket", "get");
};
