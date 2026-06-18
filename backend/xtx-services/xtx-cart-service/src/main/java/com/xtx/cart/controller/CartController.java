package com.xtx.cart.controller;

import com.xtx.api.cart.dto.AddCartDTO;
import com.xtx.api.cart.dto.CartMergeItemDTO;
import com.xtx.api.cart.dto.UpdateCartDTO;
import com.xtx.cart.service.CartAppService;
import com.xtx.cart.vo.CartVO;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.core.result.FrontResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车前端控制器
 */
@FrontController
@RestController
@RequestMapping("/member/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartAppService cartAppService;

    /**
     * 获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @GetMapping
    public FrontResponse<List<CartVO>> getCartList(@XUserId Long userId) {
        List<CartVO> list = cartAppService.getCartList(userId);
        return FrontResponse.success(list);
    }

    /**
     * 添加商品到购物车
     *
     * @param dto    添加购物车参数
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping
    public FrontResponse<Void> addCart(@RequestBody @Valid AddCartDTO dto, @XUserId Long userId) {
        cartAppService.addCartItem(userId, dto.getSkuId(), dto.getCount());
        return FrontResponse.success();
    }

    /**
     * 删除购物车商品（逻辑删除）
     * 前端发送 { ids: ["1", "2", "3"] }
     *
     * @param body   请求体，包含ids字段（SKU ID列表，String格式）
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping
    public FrontResponse<Void> deleteCart(@RequestBody Map<String, List<String>> body, @XUserId Long userId) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return FrontResponse.success();
        }
        List<Long> skuIds = ids.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        cartAppService.deleteCartItems(userId, skuIds);
        return FrontResponse.success();
    }

    /**
     * 更新购物车商品
     *
     * @param skuId   SKU ID
     * @param dto     更新参数（selected和count均为可选）
     * @param userId  用户ID
     * @return 操作结果
     */
    @PutMapping("/{skuId}")
    public FrontResponse<Void> updateCart(@PathVariable Long skuId, @RequestBody UpdateCartDTO dto, @XUserId Long userId) {
        cartAppService.updateCartItem(userId, skuId, dto.getSelected(), dto.getCount());
        return FrontResponse.success();
    }

    /**
     * 批量切换选中状态
     * 前端发送 { selected: true, ids: ["1", "2"] }
     *
     * @param body   请求体，包含selected和ids字段
     * @param userId 用户ID
     * @return 操作结果
     */
    @SuppressWarnings("unchecked")
    @PutMapping("/selected")
    public FrontResponse<Void> toggleSelect(@RequestBody Map<String, Object> body, @XUserId Long userId) {
        Boolean selected = (Boolean) body.get("selected");
        List<String> ids = (List<String>) body.get("ids");
        List<Long> skuIds = null;
        if (ids != null && !ids.isEmpty()) {
            skuIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        }
        cartAppService.toggleSelectAll(userId, selected, skuIds);
        return FrontResponse.success();
    }

    /**
     * 合并购物车
     *
     * @param items  本地购物车条目列表
     * @param userId 用户ID
     * @return 合并后的购物车列表
     */
    @PostMapping("/merge")
    public FrontResponse<List<CartVO>> mergeCart(@RequestBody List<CartMergeItemDTO> items, @XUserId Long userId) {
        List<CartVO> result = cartAppService.mergeCart(userId, items);
        return FrontResponse.success(result);
    }
}
