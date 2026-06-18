package com.xtx.payment.controller;

import com.xtx.common.core.result.ApiResponse;
import com.xtx.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.payment.dto.PayOrderDTO;
import com.xtx.payment.service.PaymentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付内部控制器（服务间调用）
 */
@RestController
@RequestMapping("/inner/payments")
@RequiredArgsConstructor
public class PayInnerController {

    private final PaymentAppService paymentAppService;

    /**
     * 创建支付订单
     *
     * @param request 创建支付订单请求
     * @return 支付订单DTO
     */
    @PostMapping("/create")
    public ApiResponse<PayOrderDTO> createPayOrder(@RequestBody CreatePayOrderRequestDTO request) {
        PayOrderDTO payOrder = paymentAppService.createPayOrder(request);
        return ApiResponse.success(payOrder);
    }

    /**
     * 根据订单编号查询支付订单
     *
     * @param orderNo 订单编号
     * @return 支付订单DTO
     */
    @GetMapping("/order/{orderNo}")
    public ApiResponse<PayOrderDTO> getByOrderNo(@PathVariable String orderNo) {
        PayOrderDTO payOrder = paymentAppService.getPayOrderByOrderNo(orderNo);
        return ApiResponse.success(payOrder);
    }
}
