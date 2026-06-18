package com.xtx.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 内容管理服务启动类
 * 负责首页内容管理，包括 Banner、新品推荐、人气推荐、专题等
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.cms", "com.xtx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xtx.api.goods")
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}
