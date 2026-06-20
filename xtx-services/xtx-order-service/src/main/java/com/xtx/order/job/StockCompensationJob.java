package com.xtx.order.job;

import com.xtx.order.service.StockCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockCompensationJob {

    private final StockCompensationService stockCompensationService;

    @Scheduled(fixedDelay = 60000)
    public void processStockCompensationTasks() {
        stockCompensationService.processRetryableTasks();
    }
}
