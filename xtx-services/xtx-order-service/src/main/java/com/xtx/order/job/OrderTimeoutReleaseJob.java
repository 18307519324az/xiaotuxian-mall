package com.xtx.order.job;

import com.xtx.order.entity.OrderInfo;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.service.OrderAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutReleaseJob {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderAppService orderAppService;

    @Scheduled(fixedDelay = 60000)
    public void releaseTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        for (OrderInfo order : orderInfoMapper.selectTimeoutPendingPayOrders(deadline)) {
            try {
                orderAppService.cancelOrderByOrderNo(order.getUserId(), order.getOrderNo(), "订单超时未支付");
            } catch (Exception e) {
                log.error("超时订单释放失败, orderNo={}", order.getOrderNo(), e);
            }
        }
    }
}
