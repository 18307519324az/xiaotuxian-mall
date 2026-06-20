package com.xtx.api.goods;

import com.xtx.api.goods.dto.GoodsSimpleDTO;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 商品服务 Feign 远程调用客户端
 * 提供 SKU 快照查询和商品基本信息查询接口
 */
@FeignClient(name = "xtx-goods-service", url = "${services.goods:http://localhost:8105}", contextId = "goodsClient", path = "/inner/goods")
public interface GoodsClient {

    /**
     * 批量查询 SKU 快照
     *
     * @param skuIds SKU ID 列表
     * @return SKU 快照列表
     */
    @PostMapping("/skus/snapshots")
    ApiResponse<List<SkuSnapshotDTO>> listSkuSnapshots(@RequestBody List<Long> skuIds);

    /**
     * 查询单个 SKU 快照
     *
     * @param skuId SKU ID
     * @return SKU 快照
     */
    @GetMapping("/skus/{skuId}")
    ApiResponse<SkuSnapshotDTO> getSkuSnapshot(@PathVariable("skuId") Long skuId);

    /**
     * 批量查询商品基本信息
     *
     * @param goodsIds 商品 ID 列表
     * @return 商品简单信息列表
     */
    @PostMapping("/batch")
    ApiResponse<List<GoodsSimpleDTO>> listGoodsByIds(@RequestBody List<Long> goodsIds);

    /**
     * 根据三级分类 ID 列表批量查询商品
     *
     * @param categoryIds 三级分类 ID 列表
     * @return 商品数据列表（含 categoryId 用于分组）
     */
    @PostMapping("/by-category-ids")
    ApiResponse<List<Map<String, Object>>> listGoodsByCategoryIds(@RequestBody List<Long> categoryIds);

    /**
     * 获取新品商品列表（供首页服务调用）
     *
     * @param limit 返回数量限制
     * @return 商品简单信息列表
     */
    @GetMapping("/new")
    ApiResponse<List<GoodsSimpleDTO>> getNewGoods(@RequestParam("limit") Integer limit);

    /**
     * 获取热销商品列表（供首页服务调用）
     *
     * @param limit 返回数量限制
     * @return 商品简单信息列表
     */
    @GetMapping("/hot")
    ApiResponse<List<GoodsSimpleDTO>> getHotGoods(@RequestParam("limit") Integer limit);

    /**
     * 根据一级分类 ID 获取商品列表（供首页服务调用）
     *
     * @param categoryId 一级分类 ID
     * @param limit      返回数量限制
     * @return 商品简单信息列表
     */
    @GetMapping("/by-top-category")
    ApiResponse<List<GoodsSimpleDTO>> getGoodsByTopCategory(
            @RequestParam("categoryId") String categoryId,
            @RequestParam("limit") Integer limit);
}
