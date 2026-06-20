package com.xtx.cms.controller;

import com.xtx.cms.service.CmsAppService;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 内容管理控制器
 * 提供首页各板块数据查询接口
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class CmsController {

    private final CmsAppService cmsAppService;

    /**
     * 获取首页品牌列表
     *
     * @param limit 限制返回数量，默认 10
     * @return 品牌列表
     */
    @GetMapping("/home/brand")
    public FrontResponse<List<Map<String, Object>>> getBrandList(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> brandList = cmsAppService.getBrandList(limit);
        return FrontResponse.success(brandList);
    }

    /**
     * 获取首页轮播图列表
     *
     * @return 轮播图列表
     */
    @GetMapping("/home/banner")
    public FrontResponse<List<Map<String, Object>>> getBanners() {
        List<Map<String, Object>> banners = cmsAppService.getBannerMaps();
        return FrontResponse.success(banners);
    }

    /**
     * 获取新品推荐商品列表
     *
     * @return 新品商品列表
     */
    @GetMapping("/home/new")
    public FrontResponse<List<Map<String, Object>>> getNewGoods() {
        List<Map<String, Object>> goods = cmsAppService.getNewGoods();
        return FrontResponse.success(goods);
    }

    /**
     * 获取人气推荐商品列表
     *
     * @return 人气商品列表
     */
    @GetMapping("/home/hot")
    public FrontResponse<List<Map<String, Object>>> getHotGoods() {
        List<Map<String, Object>> goods = cmsAppService.getHotGoods();
        return FrontResponse.success(goods);
    }

    /**
     * 获取首页商品板块数据
     *
     * @return 商品板块列表
     */
    @GetMapping("/home/goods")
    public FrontResponse<List<Map<String, Object>>> getGoodsBlock() {
        List<Map<String, Object>> block = cmsAppService.getGoodsBlock();
        return FrontResponse.success(block);
    }

    /**
     * 获取最新专题活动列表
     *
     * @return 专题活动列表
     */
    @GetMapping("/home/special")
    public FrontResponse<List<Map<String, Object>>> getSpecialList() {
        List<Map<String, Object>> specials = cmsAppService.getSpecialMaps();
        return FrontResponse.success(specials);
    }
}
