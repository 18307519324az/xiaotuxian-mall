package com.xtx.goods.service;

import java.util.List;
import java.util.Map;

/**
 * 商品应用服务接口
 * 提供商品详情、相关推荐、热门商品、SKU 查询等核心功能
 */
public interface GoodsAppService {

    /**
     * 获取商品详情
     * 聚合商品基本信息、品牌、分类、图片、详情、规格、SKU 等数据
     *
     * @param id 商品ID（字符串，内部转为 Long）
     * @return 商品详情数据（结构符合 PRD 7.4.2 要求）
     */
    Map<String, Object> getGoodsDetail(String id);

    /**
     * 获取相关推荐商品
     * 相同分类下按销量排序，排除当前商品
     *
     * @param id    当前商品ID（字符串，内部转为 Long）
     * @param limit 返回数量
     * @return 相关商品列表
     */
    List<Map<String, Object>> getRelevantGoods(String id, Integer limit);

    /**
     * 获取热门推荐商品
     *
     * @param id    当前商品ID（字符串，内部转为 Long）
     * @param type  热门类型
     * @param limit 返回数量
     * @return 热门商品列表
     */
    List<Map<String, Object>> getHotGoods(String id, Integer type, Integer limit);

    /**
     * 获取 SKU 信息
     *
     * @param skuId SKU ID（字符串，内部转为 Long）
     * @return SKU 详情
     */
    Map<String, Object> getSkuInfo(String skuId);

    /**
     * 获取 SKU 库存信息
     *
     * @param skuId SKU ID（字符串，内部转为 Long）
     * @return SKU 库存信息（含 id, stock, price, isEffective）
     */
    Map<String, Object> getSkuStock(String skuId);

    /**
     * 批量查询 SKU 快照（内部 Feign 调用）
     *
     * @param skuIds SKU ID 列表
     * @return SKU 快照数据列表
     */
    List<Map<String, Object>> listSkuSnapshots(List<Long> skuIds);

    /**
     * 根据 ID 列表批量查询商品（内部 Feign 调用）
     *
     * @param goodsIds 商品 ID 列表
     * @return 商品数据列表
     */
    List<Map<String, Object>> listGoodsByIds(List<Long> goodsIds);

    /**
     * 根据分类 ID 列表批量查询商品
     * 用于分类服务获取各分类下的商品
     *
     * @param categoryIds 分类 ID 列表（三级分类）
     * @return 商品数据列表（含 id, name, picture, price, desc）
     */
    List<Map<String, Object>> listGoodsByCategoryIds(List<Long> categoryIds);

    /**
     * 获取新品商品列表（按 sort 权重降序）
     *
     * @param limit 返回数量
     * @return 商品简单信息列表
     */
    List<Map<String, Object>> getNewGoodsList(Integer limit);

    /**
     * 获取热销商品列表（按销量降序）
     *
     * @param limit 返回数量
     * @return 商品简单信息列表
     */
    List<Map<String, Object>> getHotGoodsList(Integer limit);

    /**
     * 根据一级分类 ID 获取商品列表
     *
     * @param topCategoryId 一级分类 ID
     * @param limit         返回数量
     * @return 商品简单信息列表
     */
    List<Map<String, Object>> getGoodsByTopCategory(Long topCategoryId, Integer limit);
}
