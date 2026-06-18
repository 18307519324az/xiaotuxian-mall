package com.xtx.goods.service;

import java.util.Map;

/**
 * 商品列表应用服务接口
 * 提供分类商品列表分页查询功能
 * v1.6 切片：替代 Mock POST /category/goods/temporary
 */
public interface GoodsListAppService {

    /**
     * 分页查询分类商品列表
     * 支持按分类、关键词、品牌筛选，以及多种排序方式
     *
     * @param categoryId   分类ID（可选，String 格式）
     * @param page         页码（从 1 开始）
     * @param pageSize     每页数量
     * @param sortField    排序字段（null/publishTime/orderNum/evaluateNum/price）
     * @param sortMethod   排序方向（null/asc/desc）
     * @param inventoryOnly 是否仅显示有货
     * @param discountOnly 是否仅显示特惠
     * @param brandId      品牌ID（可选，String 格式）
     * @param keyword      关键词（可选，name 模糊搜索）
     * @return 分页结果 { counts, pageSize, page, pages, items }
     */
    Map<String, Object> getGoodsList(String categoryId, int page, int pageSize,
                                     String sortField, String sortMethod,
                                     boolean inventoryOnly, boolean discountOnly,
                                     String brandId, String keyword);
}
