package com.xtx.payment.controller;

import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.common.web.annotation.XUserId;
import com.xtx.payment.dto.MockPayDTO;
import com.xtx.payment.service.PaymentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@FrontController
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final PaymentAppService paymentAppService;

    @PostMapping("/mock")
    public FrontResponse<Void> mockPay(@RequestBody MockPayDTO dto, @XUserId Long userId) {
        if (dto.getOrderNo() != null && !dto.getOrderNo().isBlank()) {
            paymentAppService.mockPayByOrderNo(dto.getOrderNo(), userId);
        } else {
            paymentAppService.mockPay(dto.getOrderId(), userId);
        }
        return FrontResponse.success();
    }
}
