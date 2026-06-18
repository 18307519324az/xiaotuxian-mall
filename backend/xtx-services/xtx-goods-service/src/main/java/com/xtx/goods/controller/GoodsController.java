package com.xtx.goods.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.goods.service.GoodsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品前端控制器
 * 提供商品详情、相关推荐、热门商品、SKU 信息等前端接口
 * 入参全部使用 String 类型，内部转为 Long
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsAppService goodsAppService;

    /**
     * 获取商品详情
     * 包含商品基本信息、品牌、分类、图片、规格、SKU 等完整数据
     *
     * @param id 商品ID（字符串格式）
     * @return 商品详情
     */
    @GetMapping("/goods")
    public FrontResponse<Map<String, Object>> getDetail(@RequestParam String id) {
        Map<String, Object> detail = goodsAppService.getGoodsDetail(id);
        return FrontResponse.success(detail);
    }

    /**
     * 获取相关推荐商品
     *
     * @param id    当前商品ID（字符串格式）
     * @param limit 返回数量，默认 10
     * @return 相关商品列表
     */
    @GetMapping("/goods/relevant")
    public FrontResponse<List<Map<String, Object>>> getRelevant(
            @RequestParam String id,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> relevant = goodsAppService.getRelevantGoods(id, limit);
        return FrontResponse.success(relevant);
    }

    /**
     * 获取热门推荐商品
     *
     * @param id    当前商品ID（字符串格式）
     * @param type  热门类型
     * @param limit 返回数量，默认 10
     * @return 热门商品列表
     */
    @GetMapping("/goods/hot")
    public FrontResponse<List<Map<String, Object>>> getHot(
            @RequestParam String id,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> hot = goodsAppService.getHotGoods(id, type, limit);
        return FrontResponse.success(hot);
    }

    /**
     * 获取 SKU 详情信息
     *
     * @param skuId SKU ID（字符串格式）
     * @return SKU 详情
     */
    @GetMapping("/goods/sku/{skuId}")
    public FrontResponse<Map<String, Object>> getSkuInfo(@PathVariable String skuId) {
        Map<String, Object> skuInfo = goodsAppService.getSkuInfo(skuId);
        return FrontResponse.success(skuInfo);
    }

    /**
     * 获取 SKU 库存信息
     *
     * @param skuId SKU ID（字符串格式）
     * @return SKU 库存信息
     */
    @GetMapping("/goods/stock/{skuId}")
    public FrontResponse<Map<String, Object>> getStock(@PathVariable String skuId) {
        Map<String, Object> stock = goodsAppService.getSkuStock(skuId);
        return FrontResponse.success(stock);
    }
}
