package com.xtx.gateway;

import com.xtx.gateway.config.GatewaySecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 小兔鲜儿微服务网关启动类
 */
@SpringBootApplication(scanBasePackages = {"com.xtx.gateway", "com.xtx.common"})
@EnableDiscoveryClient
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
