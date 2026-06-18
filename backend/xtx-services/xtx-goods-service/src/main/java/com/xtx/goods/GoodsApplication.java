package com.xtx.goods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 * 负责商品信息管理、商品详情、SKU、规格等核心商品数据
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.goods", "com.xtx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.xtx.goods", "com.xtx.api.goods"})
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }
}
