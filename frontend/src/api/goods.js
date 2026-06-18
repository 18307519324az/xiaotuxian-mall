import request from "@/utils/request";
/**
 * 获取商品同类推荐-未传入ID为猜喜欢
 * @param {String} id - 商品ID
 * @param {Number} limit - 获取条数
 */
export const findRelGoods = (id, limit = 16) => {
  return request("/goods/relevant", "get", { id, limit });
};

/**
 * 获取热榜商品
 * @param {Number} type - 1代表24小时热销榜 2代表周热销榜 3代表总热销榜
 * @param {Number} limit - 获取个数
 */
export const findHotGoods = ({ id, type, limit = 3 }) => {
  return request("/goods/hot", "get", { id, type, limit });
};

/**
 * 获取商品的评价统计信息
 * @param {String} id - 商品ID
 */
export const findGoodsCommentInfo = (id) => {
  return request(`/goods/${id}/evaluate`, "get");
  // axios 遇见 http https 开头的地址，不会加上基准地址
  // return request(
  //   `https://mock.boxuegu.com/mock/1175/goods/${id}/evaluate`,
  //   "get"
  // );
};
// https://mock.boxuegu.com/mock/1175/goods/${id}/evaluate

/**
 * 查询商品评价列表
 * @param {String} id - 商品ID
 * @param {Object} params - 商品ID
 * @returns
 */
export const findGoodsCommentList = (id, params) => {
  // Use proxy path (not hardcoded boxuegu URL) so it goes through local mock
  return request(`/goods/${id}/evaluate/page`, "get", params);
};

/**
 * 点赞/取消点赞评价
 * @param {String} evaluateId - 评价ID
 * @param {Boolean} isPraise - 是否点赞
 * @returns
 */
export const praiseEvaluate = (evaluateId, isPraise) => {
  return request(`/goods/evaluate/${evaluateId}/praise`, "put", { isPraise });
};
