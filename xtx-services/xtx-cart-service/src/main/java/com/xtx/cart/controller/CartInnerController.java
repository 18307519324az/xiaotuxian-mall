package com.xtx.cart.controller;

import com.xtx.api.cart.dto.CartMergeItemDTO;
import com.xtx.cart.service.CartAppService;
import com.xtx.cart.vo.CartVO;
import com.xtx.common.core.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车内部控制器（服务间调用）
 */
@RestController
@RequestMapping("/inner/cart")
@RequiredArgsConstructor
public class CartInnerController {

    private final CartAppService cartAppService;

    /**
     * 获取购物车中已选中的商品
     *
     * @param userId 用户ID（请求头中获取）
     * @return 已选中的购物车商品列表
     */
    @GetMapping("/selected")
    public ApiResponse<List<CartMergeItemDTO>> getSelectedItems(@RequestHeader("X-User-Id") Long userId) {
        List<CartVO> cartList = cartAppService.getCartList(userId);
        List<CartMergeItemDTO> selectedItems = cartList.stream()
                .filter(CartVO::getSelected)
                .map(vo -> {
                    CartMergeItemDTO dto = new CartMergeItemDTO();
                    // CartVO.skuId is String, CartMergeItemDTO.skuId is Long
                    dto.setSkuId(vo.getSkuId() != null ? Long.valueOf(vo.getSkuId()) : null);
                    dto.setCount(vo.getCount());
                    dto.setSelected(vo.getSelected());
                    return dto;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(selectedItems);
    }

    /**
     * 清理购物车中指定SKU（下单后清理已购买的商品）
     *
     * @param skuIds SKU ID列表
     * @param userId 用户ID（请求头中获取）
     * @return 操作结果
     */
    @DeleteMapping("/clean")
    public ApiResponse<Void> cleanCart(@RequestParam List<Long> skuIds, @RequestHeader("X-User-Id") Long userId) {
        cartAppService.cleanCartBySkuIds(userId, skuIds);
        return ApiResponse.success();
    }
}
