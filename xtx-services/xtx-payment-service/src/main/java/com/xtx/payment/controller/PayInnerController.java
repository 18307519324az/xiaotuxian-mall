package com.xtx.payment.controller;

import com.xtx.common.core.result.ApiResponse;
import com.xtx.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.payment.dto.PayOrderDTO;
import com.xtx.payment.service.PaymentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inner/payments")
@RequiredArgsConstructor
public class PayInnerController {

    private final PaymentAppService paymentAppService;

    @PostMapping("/create")
    public ApiResponse<PayOrderDTO> createPayOrder(@RequestBody CreatePayOrderRequestDTO request) {
        PayOrderDTO payOrder = paymentAppService.createPayOrder(request);
        return ApiResponse.success(payOrder);
    }

    @GetMapping("/order/{orderNo}")
    public ApiResponse<PayOrderDTO> getByOrderNo(@PathVariable String orderNo) {
        PayOrderDTO payOrder = paymentAppService.getPayOrderByOrderNo(orderNo);
        return ApiResponse.success(payOrder);
    }

    @PostMapping("/mock/order/{orderNo}/users/{userId}")
    public ApiResponse<Void> mockPayByOrderNo(@PathVariable String orderNo,
                                              @PathVariable Long userId) {
        paymentAppService.mockPayByOrderNo(orderNo, userId);
        return ApiResponse.success();
    }
}
