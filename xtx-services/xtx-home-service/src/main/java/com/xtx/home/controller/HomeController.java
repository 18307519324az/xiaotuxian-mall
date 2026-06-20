package com.xtx.home.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页前端控制器
 * 提供首页各板块数据的前端访问接口，所有返回均使用 FrontResponse {msg, result} 格式
 */
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 获取首页轮播图列表
     *
     * @return 横幅列表，每项包含 id, imgUrl, hrefUrl, type
     */
    @GetMapping("/home/banner")
    public FrontResponse<List<Map<String, Object>>> banner() {
        List<Map<String, Object>> banners = homeService.getBanners();
        return FrontResponse.success(banners);
    }

    /**
     * 获取首页推荐品牌列表
     *
     * @param limit 返回数量限制，默认 10
     * @return 品牌列表，每项包含 id, name, picture, logo
     */
    @GetMapping("/home/brand")
    public FrontResponse<List<Map<String, Object>>> brand(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> brands = homeService.getBrands(limit);
        return FrontResponse.success(brands);
    }

    /**
     * 获取首页专题列表
     *
     * @param limit 返回数量限制，默认 10
     * @return 专题列表，每项包含 id, title, cover, summary, lowestPrice
     */
    @GetMapping("/home/special")
    public FrontResponse<List<Map<String, Object>>> special(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> specials = homeService.getSpecials(limit);
        return FrontResponse.success(specials);
    }

    /**
     * 获取首页楼层聚合数据
     * 包含各楼层分类信息和商品卡片
     *
     * @return 楼层列表，每项包含 id, name, picture, children, goods[]
     */
    @GetMapping("/home/goods")
    public FrontResponse<List<Map<String, Object>>> homeGoods() {
        List<Map<String, Object>> goods = homeService.getHomeGoods();
        return FrontResponse.success(goods);
    }

    /**
     * 获取新鲜好物商品列表
     *
     * @param limit 返回数量限制，默认 4
     * @return 商品卡片列表
     */
    @GetMapping("/home/new")
    public FrontResponse<List<Map<String, Object>>> newGoods(
            @RequestParam(required = false, defaultValue = "4") Integer limit) {
        List<Map<String, Object>> goods = homeService.getNewGoods(limit);
        return FrontResponse.success(goods);
    }

    /**
     * 获取人气推荐商品列表
     *
     * @param limit 返回数量限制，默认 4
     * @return 商品卡片列表，每项包含 id, name, desc, price, picture, alt, title
     */
    @GetMapping("/home/hot")
    public FrontResponse<List<Map<String, Object>>> hotGoods(
            @RequestParam(required = false, defaultValue = "4") Integer limit) {
        List<Map<String, Object>> goods = homeService.getHotGoods(limit);
        return FrontResponse.success(goods);
    }
}
