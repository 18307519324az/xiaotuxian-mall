package com.xtx.goods.client;

import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * 分类服务 Feign 远程调用客户端
 * 提供分类名称解析、分类链查询等接口
 */
@FeignClient(name = "xtx-category-service", url = "${services.category:http://localhost:8104}", contextId = "categoryClient")
public interface CategoryClient {

    /**
     * 根据三级分类 ID 获取完整分类链（一级 → 二级 → 三级）
     *
     * @param categoryId 三级分类 ID
     * @return 分类链列表 [{id, name, level}]
     */
    @GetMapping("/inner/categories/chain/{categoryId}")
    ApiResponse<List<Map<String, Object>>> getCategoryChain(@PathVariable("categoryId") Long categoryId);
}
