package com.xtx.payment.controller;

import com.xtx.common.web.annotation.FrontController;
import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.payment.dto.MockPayDTO;
import com.xtx.payment.service.PaymentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付前端控制器
 */
@FrontController
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final PaymentAppService paymentAppService;

    /**
     * 模拟支付（开发测试用）
     *
     * @param dto    模拟支付参数
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/mock")
    public FrontResponse<Void> mockPay(@RequestBody MockPayDTO dto, @XUserId Long userId) {
        paymentAppService.mockPay(dto.getOrderId(), userId);
        return FrontResponse.success();
    }
}
