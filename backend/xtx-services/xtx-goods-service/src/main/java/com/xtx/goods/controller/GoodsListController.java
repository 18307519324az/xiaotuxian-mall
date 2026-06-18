package com.xtx.goods.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.goods.service.GoodsListAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品列表前端控制器
 * 提供分类商品列表的分页查询接口
 * v1.6 切片：替代 Mock POST /category/goods/temporary
 *
 * 入参全部使用 String/基本类型，内部转换
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class GoodsListController {

    private final GoodsListAppService goodsListAppService;

    /**
     * 分页查询分类商品列表
     * 支持按分类ID、关键词、品牌筛选，多种排序方式
     * 返回格式与 Mock toGoodsCard() 兼容
     *
     * @param params 请求参数
     * @return 分页商品列表 { counts, pageSize, page, pages, items }
     */
    @PostMapping("/category/goods/temporary")
    public FrontResponse<Map<String, Object>> getGoodsList(@RequestBody Map<String, Object> params) {
        // 解析参数（与 Mock 参数名对齐）
        String categoryId = params.get("categoryId") != null
                ? String.valueOf(params.get("categoryId")) : null;
        int page = params.get("page") != null
                ? Integer.parseInt(String.valueOf(params.get("page"))) : 1;
        int pageSize = params.get("pageSize") != null
                ? Integer.parseInt(String.valueOf(params.get("pageSize"))) : 20;
        String sortField = params.get("sortField") != null
                ? String.valueOf(params.get("sortField")) : null;
        String sortMethod = params.get("sortMethod") != null
                ? String.valueOf(params.get("sortMethod")) : null;
        boolean inventoryOnly = Boolean.parseBoolean(
                String.valueOf(params.getOrDefault("inventory", false)));
        boolean discountOnly = Boolean.parseBoolean(
                String.valueOf(params.getOrDefault("onlyDiscount", false)));
        // brandId 来自 SubFilter 的 getFilterParams()
        String brandId = params.get("brandId") != null
                ? String.valueOf(params.get("brandId")) : null;
        // keyword 为预留参数（前端搜索框目前未使用）
        String keyword = params.get("keyword") != null
                ? String.valueOf(params.get("keyword")) : null;

        Map<String, Object> result = goodsListAppService.getGoodsList(
                categoryId, page, pageSize,
                sortField, sortMethod,
                inventoryOnly, discountOnly,
                brandId, keyword
        );

        return FrontResponse.success(result);
    }
}
