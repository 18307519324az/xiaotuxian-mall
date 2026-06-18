package com.xtx.order.controller;

import com.xtx.common.web.annotation.FrontController;
import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.core.model.PageResult;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.service.OrderAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单前端控制器
 */
@FrontController
@RestController
@RequestMapping("/member/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderAppService orderAppService;

    /**
     * 获取订单预览（从购物车选中商品生成预览）
     *
     * @param userId 用户ID
     * @return 订单预览数据
     */
    @GetMapping("/pre")
    public FrontResponse<Map<String, Object>> getPreview(@XUserId Long userId) {
        Map<String, Object> preview = orderAppService.getOrderPreview(userId);
        return FrontResponse.success(preview);
    }

    /**
     * 获取复购预览
     *
     * @param userId  用户ID
     * @param orderId 原订单ID
     * @return 复购预览数据
     */
    @GetMapping("/repurchase/{orderId}")
    public FrontResponse<Map<String, Object>> getRepurchasePreview(@XUserId Long userId,
                                                                      @PathVariable Long orderId) {
        Map<String, Object> preview = orderAppService.getRepurchasePreview(userId, orderId);
        return FrontResponse.success(preview);
    }

    /**
     * 提交订单
     *
     * @param userId 用户ID
     * @param dto    提交订单参数
     * @return 订单结果（orderId, orderNo, payMoney）
     */
    @PostMapping
    public FrontResponse<Map<String, Object>> submitOrder(@XUserId Long userId,
                                                            @RequestBody SubmitOrderDTO dto) {
        Map<String, Object> result = orderAppService.submitOrder(userId, dto);
        return FrontResponse.success(result);
    }

    /**
     * 获取订单列表（分页）
     *
     * @param userId     用户ID
     * @param page       页码
     * @param pageSize   每页大小
     * @param orderState 订单状态（可选）
     * @return 分页订单列表
     */
    @GetMapping
    public FrontResponse<PageResult<Map<String, Object>>> getOrderList(@XUserId Long userId,
                                                                         @RequestParam Integer page,
                                                                         @RequestParam Integer pageSize,
                                                                         @RequestParam(required = false) Integer orderState) {
        PageResult<Map<String, Object>> result = orderAppService.getOrderList(userId, page, pageSize, orderState);
        return FrontResponse.success(result);
    }

    /**
     * 获取订单详情
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    public FrontResponse<Map<String, Object>> getOrderDetail(@XUserId Long userId,
                                                               @PathVariable Long orderId) {
        Map<String, Object> detail = orderAppService.getOrderDetail(userId, orderId);
        return FrontResponse.success(detail);
    }

    /**
     * 取消订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param body    请求体，包含cancelReason
     * @return 操作结果
     */
    @PutMapping("/{orderId}/cancel")
    public FrontResponse<Void> cancelOrder(@XUserId Long userId,
                                             @PathVariable Long orderId,
                                             @RequestBody Map<String, String> body) {
        String reason = body.get("cancelReason");
        orderAppService.cancelOrder(userId, orderId, reason);
        return FrontResponse.success();
    }

    /**
     * 删除订单（软删除）
     *
     * @param userId 用户ID
     * @param body   请求体，包含ids字段
     * @return 操作结果
     */
    @DeleteMapping
    public FrontResponse<Void> deleteOrder(@XUserId Long userId,
                                             @RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null) {
            for (Long orderId : ids) {
                orderAppService.deleteOrder(userId, orderId);
            }
        }
        return FrontResponse.success();
    }

    /**
     * 确认收货
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/receipt")
    public FrontResponse<Void> confirmReceipt(@XUserId Long userId,
                                                @PathVariable Long orderId) {
        orderAppService.confirmReceipt(userId, orderId);
        return FrontResponse.success();
    }
}
