package com.xtx.api.payment;

import com.xtx.api.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.api.payment.dto.PayOrderDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "xtx-payment-service",
        url = "${services.payment:http://localhost:8109}",
        contextId = "paymentClient",
        path = "/inner/payments"
)
public interface PaymentClient {

    @PostMapping("/create")
    ApiResponse<PayOrderDTO> createPayOrder(@RequestBody CreatePayOrderRequestDTO request);

    @GetMapping("/order/{orderNo}")
    ApiResponse<PayOrderDTO> getPayOrderByOrderNo(@PathVariable("orderNo") String orderNo);

    @GetMapping("/{payNo}")
    ApiResponse<PayOrderDTO> getPayOrderByPayNo(@PathVariable("payNo") String payNo);

    @PostMapping("/mock/order/{orderNo}/users/{userId}")
    ApiResponse<Void> mockPayByOrderNo(@PathVariable("orderNo") String orderNo,
                                       @PathVariable("userId") Long userId);
}
