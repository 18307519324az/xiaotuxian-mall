package com.xtx.common.security.config;

import com.xtx.common.security.properties.JwtProperties;
import com.xtx.common.security.util.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全模块自动配置。
 * 启用 JWT 配置属性并声明 JwtUtil Bean。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    /**
     * 声明 JwtUtil Bean。
     * 若容器中已存在则跳过，避免重复创建。
     *
     * @param jwtProperties JWT 配置属性
     * @return JwtUtil 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }
}
