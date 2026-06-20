package com.xtx.home.service;

import java.util.List;
import java.util.Map;

/**
 * 首页应用服务接口
 * 提供首页各板块数据的查询功能，包括横幅、品牌、专题、楼层商品、新品、热销
 */
public interface HomeService {

    /**
     * 获取首页轮播图列表
     *
     * @return 横幅列表，每项包含 id, imgUrl, hrefUrl, type
     */
    List<Map<String, Object>> getBanners();

    /**
     * 获取首页推荐品牌列表
     *
     * @param limit 返回数量限制
     * @return 品牌列表，每项包含 id, name, picture, logo
     */
    List<Map<String, Object>> getBrands(Integer limit);

    /**
     * 获取首页专题列表
     *
     * @param limit 返回数量限制
     * @return 专题列表，每项包含 id, title, cover, summary, lowestPrice
     */
    List<Map<String, Object>> getSpecials(Integer limit);

    /**
     * 获取首页楼层聚合数据
     * 从 home_floor 表获取楼层配置，通过 Feign 调用 goods-service 获取各楼层商品
     *
     * @return 楼层列表，每项包含 id, name, picture, children, goods[]
     */
    List<Map<String, Object>> getHomeGoods();

    /**
     * 获取新鲜好物商品列表
     * 通过 Feign 调用 goods-service 获取新品商品卡片
     *
     * @param limit 返回数量限制
     * @return 商品卡片列表，每项包含 id, name, desc, price, picture
     */
    List<Map<String, Object>> getNewGoods(Integer limit);

    /**
     * 获取人气推荐商品列表
     * 通过 Feign 调用 goods-service 获取热销商品卡片
     *
     * @param limit 返回数量限制
     * @return 商品卡片列表，每项包含 id, name, desc, price, picture, alt, title
     */
    List<Map<String, Object>> getHotGoods(Integer limit);
}
