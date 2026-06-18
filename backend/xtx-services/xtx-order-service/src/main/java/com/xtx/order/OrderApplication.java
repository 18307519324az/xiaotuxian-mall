package com.xtx.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.order", "com.xtx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xtx.api")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
