package com.xtx.category.controller;

import com.xtx.category.service.CategoryAppService;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 分类控制器
 * 提供商品分类展示、筛选等接口
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryAppService categoryAppService;

    /**
     * 获取首页头部分类树
     * 返回三级分类的树形结构，每个一级分类附带商品卡片
     *
     * @return 分类树列表
     */
    @GetMapping("/home/category/head")
    public FrontResponse<List<Map<String, Object>>> getHeadCategory() {
        List<Map<String, Object>> categoryTree = categoryAppService.getHeadCategoryTree();
        return FrontResponse.success(categoryTree);
    }

    /**
     * 获取一级分类详情
     * 包含分类基本信息、轮播图及二级子分类（含三级分类和商品）
     *
     * @param id 一级分类ID（字符串格式）
     * @return 分类详情
     */
    @GetMapping("/category")
    public FrontResponse<Map<String, Object>> getTopCategory(@RequestParam String id) {
        Map<String, Object> detail = categoryAppService.getTopCategoryDetail(id);
        return FrontResponse.success(detail);
    }

    /**
     * 获取二级分类筛选条件
     * 包含三级分类筛选项及品牌列表
     *
     * @param id 二级分类ID（字符串格式）
     * @return 筛选条件数据
     */
    @GetMapping("/category/sub/filter")
    public FrontResponse<Map<String, Object>> getSubFilter(@RequestParam String id) {
        Map<String, Object> filter = categoryAppService.getSubCategoryFilter(id);
        return FrontResponse.success(filter);
    }

    /**
     * 获取分类下的商品列表（临时接口）
     * 支持分页查询和筛选
     *
     * @param params 查询参数（categoryId, page, pageSize 等）
     * @return 分页商品数据
     */
    @PostMapping("/category/goods/temporary")
    public FrontResponse<Object> getCategoryGoods(@RequestBody Map<String, Object> params) {
        Object result = categoryAppService.getCategoryGoods(params);
        return FrontResponse.success(result);
    }
}
