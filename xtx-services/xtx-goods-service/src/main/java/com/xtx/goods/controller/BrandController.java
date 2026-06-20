package com.xtx.goods.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.goods.service.BrandAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌前端控制器
 * 提供品牌详情、品牌下商品列表等只读前端接口
 * 入参全部使用 String 类型，内部转为 Long
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class BrandController {

    private final BrandAppService brandAppService;

    /**
     * 获取品牌列表（首页品牌馆使用）
     * 返回字段兼容 Mock 基线的 GET /brand 格式
     *
     * @param limit 可选限制条数
     * @return 品牌列表
     */
    @GetMapping("/brand")
    public FrontResponse<List<Map<String, Object>>> getBrandList(@RequestParam(required = false) Integer limit) {
        List<Map<String, Object>> brands = brandAppService.listAllBrands(limit);
        return FrontResponse.success(brands);
    }

    /**
     * 获取品牌详情（含品牌下商品列表）
     * 返回字段兼容 Mock 基线，缺失字段通过 putIfAbsent 兜底
     *
     * @param id 品牌ID（字符串格式）
     * @return 品牌详情
     */
    @GetMapping("/brand/{id}")
    public FrontResponse<Map<String, Object>> getBrandDetail(@PathVariable String id) {
        Map<String, Object> detail = brandAppService.getBrandDetail(id);
        return FrontResponse.success(detail);
    }

    /**
     * 获取品牌下商品列表
     *
     * @param id 品牌ID（字符串格式）
     * @return 品牌商品列表
     */
    @GetMapping("/brand/{id}/goods")
    public FrontResponse<List<Map<String, Object>>> getBrandGoods(@PathVariable String id) {
        List<Map<String, Object>> goods = brandAppService.getBrandGoods(id);
        return FrontResponse.success(goods);
    }
}
