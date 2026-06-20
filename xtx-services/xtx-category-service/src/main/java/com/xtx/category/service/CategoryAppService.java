package com.xtx.category.service;

import java.util.List;
import java.util.Map;

/**
 * 分类应用服务接口
 * 提供分类树、分类详情、分类筛选等功能
 */
public interface CategoryAppService {

    /**
     * 获取首页头部分类树（三层结构）
     * 数据缓存 30 分钟
     *
     * @return 分类树列表
     */
    List<Map<String, Object>> getHeadCategoryTree();

    /**
     * 获取一级分类详情
     * 包含一级分类基本信息、轮播图和二级分类列表（含三级分类和商品）
     *
     * @param id 一级分类ID（字符串格式）
     * @return 一级分类详情 Map
     */
    Map<String, Object> getTopCategoryDetail(String id);

    /**
     * 获取二级分类筛选条件
     * 包含二级分类信息、三级分类列表（作为筛选项）及关联品牌
     *
     * @param id 二级分类ID（字符串格式）
     * @return 子分类筛选数据 Map
     */
    Map<String, Object> getSubCategoryFilter(String id);

    /**
     * 获取分类下的商品列表（简化分页）
     *
     * @param params 查询参数，包含分类ID、分页等信息
     * @return 分页商品数据
     */
    Object getCategoryGoods(Map<String, Object> params);
}
