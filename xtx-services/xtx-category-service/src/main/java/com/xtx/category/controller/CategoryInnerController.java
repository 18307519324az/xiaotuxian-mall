package com.xtx.category.controller;

import com.xtx.category.entity.Category;
import com.xtx.category.mapper.CategoryMapper;
import com.xtx.common.core.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类内部控制器（Feign 调用）
 * 提供微服务间调用的分类数据查询接口
 */
@RestController
@RequiredArgsConstructor
public class CategoryInnerController {

    private final CategoryMapper categoryMapper;

    /**
     * 根据分类 ID 列表批量查询分类名称
     *
     * @param ids 分类 ID 列表（字符串格式）
     * @return 分类信息列表 [{id, name, level}]
     */
    @PostMapping("/inner/categories/batch")
    public ApiResponse<List<Map<String, Object>>> getCategoryNames(@RequestBody List<String> ids) {
        List<Long> longIds = new ArrayList<>();
        for (String id : ids) {
            try { longIds.add(Long.parseLong(id)); } catch (NumberFormatException ignored) {}
        }
        List<Category> categories = categoryMapper.selectListByIds(longIds);
        List<Map<String, Object>> result = categories.stream().map(cat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(cat.getId()));
            map.put("name", cat.getName());
            map.put("level", cat.getLevel());
            map.put("parentId", String.valueOf(cat.getParentId()));
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 根据三级分类 ID 获取完整分类链（三级 → 二级 → 一级）
     *
     * @param categoryId 三级分类 ID（字符串格式）
     * @return 分类链列表 [{id, name, level}]，从一级到三级
     */
    @GetMapping("/inner/categories/chain/{categoryId}")
    public ApiResponse<List<Map<String, Object>>> getCategoryChain(@PathVariable String categoryId) {
        List<Map<String, Object>> chain = new ArrayList<>();
        Long currentId;
        try { currentId = Long.parseLong(categoryId); } catch (NumberFormatException e) {
            return ApiResponse.success(chain);
        }
        List<Category> allCategories = categoryMapper.selectAllEnabled();
        // Build lookup map
        Map<Long, Category> catMap = new HashMap<>();
        for (Category c : allCategories) {
            catMap.put(c.getId(), c);
        }
        // Walk up the chain
        java.util.Stack<Map<String, Object>> stack = new java.util.Stack<>();
        while (currentId != null && currentId != 0) {
            Category cat = catMap.get(currentId);
            if (cat == null) break;
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(cat.getId()));
            map.put("name", cat.getName());
            map.put("level", cat.getLevel());
            stack.push(map);
            currentId = cat.getParentId();
        }
        // Stack gives us top-down order (level1 → level2 → level3)
        while (!stack.isEmpty()) {
            chain.add(stack.pop());
        }
        return ApiResponse.success(chain);
    }
}
