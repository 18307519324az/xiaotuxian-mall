import request from "@/utils/request";

/**
 * 搜索商品
 * @param {String} keyword - 搜索关键词
 * @param {Number} page - 页码（默认1）
 * @param {Number} pageSize - 每页条数（默认20）
 * @returns {Promise}
 */
export const searchGoods = (keyword, page = 1, pageSize = 20) => {
  return request("/search", "get", { keyword, page, pageSize });
};
