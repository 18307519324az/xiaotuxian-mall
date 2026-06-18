package com.xtx.goods.controller;

import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.goods.entity.Goods;
import com.xtx.goods.entity.GoodsSku;
import com.xtx.goods.mapper.GoodsMapper;
import com.xtx.goods.mapper.GoodsSkuMapper;
import com.xtx.goods.service.GoodsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品内部控制器（Feign 调用）
 * 提供微服务间调用的商品和 SKU 数据查询接口
 */
@RestController
@RequiredArgsConstructor
public class GoodsInnerController {

    private final GoodsAppService goodsAppService;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsMapper goodsMapper;

    /**
     * 批量查询 SKU 快照数据（供订单等服务调用）
     *
     * @param skuIds SKU ID 列表
     * @return SKU 快照数据
     */
    @PostMapping("/inner/goods/skus/snapshots")
    public ApiResponse<List<SkuSnapshotDTO>> listSkusSnapshots(@RequestBody List<Long> skuIds) {
        List<GoodsSku> skus = goodsSkuMapper.selectListByQuery(
                QueryWrapper.create().in("id", skuIds).eq("status", 1));
        List<SkuSnapshotDTO> result = skus.stream().map(sku -> {
            Goods goods = goodsMapper.selectOneById(sku.getGoodsId());
            SkuSnapshotDTO dto = new SkuSnapshotDTO();
            dto.setSkuId(sku.getId());
            dto.setGoodsId(sku.getGoodsId());
            dto.setSkuCode(sku.getSkuCode());
            // price/oldPrice: Integer 分 → BigDecimal 元
            dto.setPrice(centsToYuan(sku.getPrice()));
            dto.setOldPrice(centsToYuan(sku.getOldPrice()));
            dto.setNowPrice(centsToYuan(sku.getPrice()));
            dto.setPicture(sku.getPicture());
            dto.setIsEffective(sku.getIsEffective());
            dto.setStatus(sku.getStatus());
            dto.setStock(sku.getInventory());
            dto.setAttrsText(sku.getAttrsText());
            dto.setGoodsName(goods != null ? goods.getName() : null);
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 查询单个 SKU 快照（供购物车等服务调用）
     *
     * @param skuId SKU ID
     * @return SKU 快照
     */
    @GetMapping("/inner/goods/skus/{skuId}")
    public ApiResponse<SkuSnapshotDTO> getSkuSnapshot(@PathVariable Long skuId) {
        GoodsSku sku = goodsSkuMapper.selectOneById(skuId);
        if (sku == null) {
            return ApiResponse.success(null);
        }
        Goods goods = goodsMapper.selectOneById(sku.getGoodsId());
        SkuSnapshotDTO dto = new SkuSnapshotDTO();
        dto.setSkuId(sku.getId());
        dto.setGoodsId(sku.getGoodsId());
        dto.setSkuCode(sku.getSkuCode());
        dto.setPrice(centsToYuan(sku.getPrice()));
        dto.setOldPrice(centsToYuan(sku.getOldPrice()));
        dto.setNowPrice(centsToYuan(sku.getPrice()));
        dto.setPicture(sku.getPicture());
        dto.setIsEffective(sku.getIsEffective());
        dto.setStatus(sku.getStatus());
        dto.setStock(sku.getInventory());
        dto.setAttrsText(sku.getAttrsText());
        dto.setGoodsName(goods != null ? goods.getName() : null);
        return ApiResponse.success(dto);
    }

    /**
     * 将 Integer 分转为 BigDecimal 元
     */
    private BigDecimal centsToYuan(Integer cents) {
        if (cents == null) return null;
        return BigDecimal.valueOf(cents, 2);
    }

    /**
     * 批量查询商品基本信息（供 CMS 等服务调用）
     *
     * @param goodsIds 商品 ID 列表
     * @return 商品数据列表
     */
    @PostMapping("/inner/goods/batch")
    public ApiResponse<List<Map<String, Object>>> listGoodsByIds(@RequestBody List<Long> goodsIds) {
        List<Map<String, Object>> goodsList = goodsAppService.listGoodsByIds(goodsIds);
        return ApiResponse.success(goodsList);
    }

    /**
     * 根据三级分类 ID 列表批量查询商品（供分类服务调用）
     *
     * @param categoryIds 三级分类 ID 列表
     * @return 商品数据列表（含 categoryId 用于分组）
     */
    @PostMapping("/inner/goods/by-category-ids")
    public ApiResponse<List<Map<String, Object>>> listGoodsByCategoryIds(@RequestBody List<Long> categoryIds) {
        List<Map<String, Object>> goodsList = goodsAppService.listGoodsByCategoryIds(categoryIds);
        return ApiResponse.success(goodsList);
    }

    /**
     * 获取新品商品列表（供首页服务调用）
     * 按 sort 权重降序排列，取前 N 条
     *
     * @param limit 返回数量限制，默认 4
     * @return 商品简单信息列表
     */
    @GetMapping("/inner/goods/new")
    public ApiResponse<List<Map<String, Object>>> getNewGoods(
            @RequestParam(required = false, defaultValue = "4") Integer limit) {
        List<Map<String, Object>> goodsList = goodsAppService.getNewGoodsList(limit);
        return ApiResponse.success(goodsList);
    }

    /**
     * 获取热销商品列表（供首页服务调用）
     * 按销量降序排列，取前 N 条
     *
     * @param limit 返回数量限制，默认 4
     * @return 商品简单信息列表
     */
    @GetMapping("/inner/goods/hot")
    public ApiResponse<List<Map<String, Object>>> getHotGoods(
            @RequestParam(required = false, defaultValue = "4") Integer limit) {
        List<Map<String, Object>> goodsList = goodsAppService.getHotGoodsList(limit);
        return ApiResponse.success(goodsList);
    }

    /**
     * 根据一级分类 ID 获取商品列表（供首页服务调用）
     *
     * @param categoryId 一级分类 ID
     * @param limit      返回数量限制，默认 8
     * @return 商品简单信息列表
     */
    @GetMapping("/inner/goods/by-top-category")
    public ApiResponse<List<Map<String, Object>>> getGoodsByTopCategory(
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(required = false, defaultValue = "8") Integer limit) {
        List<Map<String, Object>> goodsList = goodsAppService.getGoodsByTopCategory(categoryId, limit);
        return ApiResponse.success(goodsList);
    }
}
