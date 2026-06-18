package com.xtx.api.payment;

import com.xtx.api.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.api.payment.dto.PayOrderDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付服务 Feign 远程调用客户端
 * 提供支付订单创建与查询接口
 */
@FeignClient(name = "xtx-payment-service", url = "${services.payment:http://localhost:8109}", contextId = "paymentClient", path = "/inner/payments")
public interface PaymentClient {

    /**
     * 创建支付订单
     *
     * @param request 创建支付订单请求
     * @return 支付订单信息
     */
    @PostMapping("/create")
    ApiResponse<PayOrderDTO> createPayOrder(@RequestBody CreatePayOrderRequestDTO request);

    /**
     * 根据业务订单号查询支付订单
     *
     * @param orderNo 业务订单编号
     * @return 支付订单信息
     */
    @GetMapping("/order/{orderNo}")
    ApiResponse<PayOrderDTO> getPayOrderByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 根据支付单号查询支付订单
     *
     * @param payNo 支付单编号
     * @return 支付订单信息
     */
    @GetMapping("/{payNo}")
    ApiResponse<PayOrderDTO> getPayOrderByPayNo(@PathVariable("payNo") String payNo);
}
