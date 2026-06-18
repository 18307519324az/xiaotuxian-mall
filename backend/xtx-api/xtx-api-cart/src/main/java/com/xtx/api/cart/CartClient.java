package com.xtx.api.cart;

import com.xtx.api.cart.dto.CartMergeItemDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 购物车服务 Feign 远程调用客户端
 * 提供购物车数据的远程操作接口（下单成功后清理已购商品）
 */
@FeignClient(name = "xtx-cart-service", url = "${services.cart:http://localhost:8107}", contextId = "cartClient", path = "/inner/cart")
public interface CartClient {

    /**
     * 获取购物车中已选中的商品
     *
     * @param userId 用户 ID（从请求头获取）
     * @return 已选中的购物车商品列表
     */
    @GetMapping("/selected")
    ApiResponse<List<CartMergeItemDTO>> getSelectedItems(@RequestHeader("X-User-Id") Long userId);

    /**
     * 按 SKU ID 列表清理购物车
     * 下单成功后调用，删除已结算的购物车项
     *
     * @param skuIds 待清理的 SKU ID 列表
     * @param userId 用户 ID（从请求头获取）
     * @return 无返回值
     */
    @DeleteMapping("/clean")
    ApiResponse<Void> cleanCartBySkuIds(@RequestParam("skuIds") List<Long> skuIds,
                                        @RequestHeader("X-User-Id") Long userId);
}
