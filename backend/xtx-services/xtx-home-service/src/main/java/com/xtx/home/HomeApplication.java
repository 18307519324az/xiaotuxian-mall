package com.xtx.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 首页服务启动类
 * 提供首页各板块数据的查询接口，包括广告横幅、品牌、专题、楼层商品等
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.xtx.common", "com.xtx.home"})
@EnableFeignClients(basePackages = {"com.xtx.api.goods", "com.xtx.api.category"})
public class HomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeApplication.class, args);
    }
}
