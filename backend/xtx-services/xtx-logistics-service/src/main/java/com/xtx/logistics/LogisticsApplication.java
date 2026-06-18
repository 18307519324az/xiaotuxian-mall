package com.xtx.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 物流服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.logistics", "com.xtx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xtx.api")
public class LogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }
}
