package com.xtx.cms.service;

import com.xtx.cms.entity.HomeBanner;
import com.xtx.cms.entity.Special;

import java.util.List;
import java.util.Map;

/**
 * 内容管理应用服务接口
 * 提供首页各板块内容的查询与组装
 */
public interface CmsAppService {

    /**
     * 获取品牌列表（静态 PRD 模拟数据）
     *
     * @param limit 限制返回数量
     * @return 品牌列表数据
     */
    List<Map<String, Object>> getBrandList(Integer limit);

    /**
     * 获取首页轮播图列表
     * 查询已启用的轮播图并缓存 10 分钟
     *
     * @return 轮播图列表
     */
    List<HomeBanner> getBanners();

    /**
     * 获取首页轮播图列表（Map 格式，字段适配前端）
     * 将 linkUrl 映射为 hrefUrl，供前端 XtxCarousel 使用
     *
     * @return 轮播图 Map 列表
     */
    List<Map<String, Object>> getBannerMaps();

    /**
     * 获取新品推荐商品列表
     * 按 NEW 类型面板查询，通过 GoodsClient 获取商品详情
     *
     * @return 新品商品列表
     */
    List<Map<String, Object>> getNewGoods();

    /**
     * 获取人气推荐商品列表
     * 按 HOT 类型面板查询，通过 GoodsClient 获取商品详情
     *
     * @return 人气商品列表
     */
    List<Map<String, Object>> getHotGoods();

    /**
     * 获取首页商品板块数据
     * 包含多个商品分组的综合数据
     *
     * @return 商品板块数据
     */
    List<Map<String, Object>> getGoodsBlock();

    /**
     * 获取最新专题活动列表（实体）
     *
     * @return 专题活动实体列表
     */
    List<Special> getSpecialList();

    /**
     * 获取最新专题活动列表（Map 格式，适配前端）
     * 字段映射：subtitle→summary, 补充 lowestPrice/collectNum/viewNum/replyNum
     *
     * @return 专题活动 Map 列表
     */
    List<Map<String, Object>> getSpecialMaps();
}
