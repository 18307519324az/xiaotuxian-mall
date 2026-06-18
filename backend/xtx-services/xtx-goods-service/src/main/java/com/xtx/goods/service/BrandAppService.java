package com.xtx.goods.service;

import java.util.List;
import java.util.Map;

/**
 * 品牌应用服务接口
 * 提供品牌详情、品牌下商品列表等只读查询功能
 */
public interface BrandAppService {

    /**
     * 获取品牌详情（含品牌下商品列表）
     * 返回字段兼容 Mock 基线，缺失字段通过 putIfAbsent 兜底
     *
     * @param id 品牌ID（字符串，内部转为 Long）
     * @return 品牌详情（含 id, name, logo, picture, place, desc, story, goods 等）
     */
    Map<String, Object> getBrandDetail(String id);

    /**
     * 获取品牌下商品列表
     *
     * @param brandId 品牌ID（字符串，内部转为 Long）
     * @return 商品简单信息列表
     */
    List<Map<String, Object>> getBrandGoods(String brandId);

    /**
     * 获取所有品牌列表（首页品牌馆使用）
     * 返回字段兼容 Mock 基线的 GET /brand 格式
     *
     * @param limit 可选限制条数
     * @return 品牌简单信息列表
     */
    List<Map<String, Object>> listAllBrands(Integer limit);
}
