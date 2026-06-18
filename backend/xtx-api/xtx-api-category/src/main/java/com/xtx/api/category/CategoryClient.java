package com.xtx.api.category;

import com.xtx.api.category.vo.CategoryHeadVO;
import com.xtx.api.category.vo.CategoryTopVO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 分类服务 Feign 远程调用客户端
 * 供其他微服务（如 home-service）内部调用分类数据
 */
@FeignClient(name = "xtx-category-service", url = "${services.category:http://localhost:8104}", contextId = "categoryClient", path = "/inner/categories")
public interface CategoryClient {

    /**
     * 根据分类 ID 列表批量查询分类名称
     *
     * @param ids 分类 ID 列表
     * @return 分类信息列表 [{id, name, level}]
     */
    @PostMapping("/batch")
    ApiResponse<List<Map<String, Object>>> getCategoryNames(@RequestBody List<String> ids);

    /**
     * 根据三级分类 ID 获取完整分类链
     *
     * @param categoryId 三级分类 ID
     * @return 分类链列表 [{id, name, level}]，从一级到三级
     */
    @GetMapping("/chain")
    ApiResponse<List<Map<String, Object>>> getCategoryChain(@RequestParam("categoryId") String categoryId);
}
