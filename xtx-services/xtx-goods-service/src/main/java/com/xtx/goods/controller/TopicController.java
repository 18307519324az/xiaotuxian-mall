package com.xtx.goods.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.goods.service.TopicAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 专题前端控制器
 * 提供专题详情、专题商品列表等只读前端接口
 * 入参全部使用 String 类型，兼容 Mock 字符串 ID（如 v0.9.7-topic-kitchen）
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicAppService topicAppService;

    /**
     * 获取专题详情（含专题下商品列表）
     * 返回字段兼容 Mock 基线，封面图通过 resolvedCover + coverSource 解析
     *
     * @param id 专题ID（字符串格式，兼容 Mock 如 v0.9.7-topic-kitchen）
     * @return 专题详情
     */
    @GetMapping("/topic/{id}")
    public FrontResponse<Map<String, Object>> getTopicDetail(@PathVariable String id) {
        Map<String, Object> detail = topicAppService.getTopicDetail(id);
        return FrontResponse.success(detail);
    }

    /**
     * 获取专题下商品列表
     *
     * @param id 专题ID
     * @return 专题商品列表
     */
    @GetMapping("/topic/{id}/goods")
    public FrontResponse<List<Map<String, Object>>> getTopicGoods(@PathVariable String id) {
        List<Map<String, Object>> goods = topicAppService.getTopicGoods(id);
        return FrontResponse.success(goods);
    }
}
