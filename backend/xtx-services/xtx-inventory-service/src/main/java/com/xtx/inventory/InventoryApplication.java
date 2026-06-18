package com.xtx.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存服务启动类
 * 负责商品库存管理、库存预占、释放与确认扣减等库存核心功能
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.inventory", "com.xtx.common"})
@EnableDiscoveryClient
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
