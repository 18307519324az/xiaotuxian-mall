package com.xtx.comment.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.comment.service.CommentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class CommentController {

    private final CommentAppService commentAppService;

    /**
     * 获取商品评价统计信息
     *
     * @param id 商品ID
     * @return 评价统计数据
     */
    @GetMapping("/{id}/evaluate")
    public FrontResponse<Map<String, Object>> getCommentStats(@PathVariable Long id) {
        Map<String, Object> stats = commentAppService.getCommentStats(id);
        return FrontResponse.success(stats);
    }

    /**
     * 分页查询商品评价
     *
     * @param id         商品ID
     * @param page       页码
     * @param pageSize   每页大小
     * @param hasPicture 是否有图筛选（可选）
     * @param tag        标签筛选（可选）
     * @param orderBy    排序方式（可选，默认按时间）
     * @return 评价分页列表
     */
    @GetMapping("/{id}/evaluate/page")
    public FrontResponse<Map<String, Object>> getCommentPage(@PathVariable Long id,
                                                               @RequestParam Integer page,
                                                               @RequestParam Integer pageSize,
                                                               @RequestParam(required = false) Integer hasPicture,
                                                               @RequestParam(required = false) String tag,
                                                               @RequestParam(required = false, defaultValue = "create_time") String orderBy) {
        Map<String, Object> result = commentAppService.getCommentPage(id, page, pageSize, hasPicture, tag, orderBy);
        return FrontResponse.success(result);
    }
}
