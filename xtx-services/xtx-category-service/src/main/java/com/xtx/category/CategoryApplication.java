package com.xtx.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 分类服务启动类
 * 负责商品分类的树形结构展示、分类筛选等功能
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.category", "com.xtx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xtx.api.goods")
public class CategoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategoryApplication.class, args);
    }
}
