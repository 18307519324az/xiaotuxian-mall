package com.xtx.common.openapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 接口文档配置。
 * 集成 Knife4j 与 SpringDoc，为前端 API 提供分组文档及全局 JWT 认证支持。
 */
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    /**
     * 用户端 API 分组。
     * 匹配 /api/v1/** 路径的接口归入该分组。
     *
     * @return GroupedOpenApi 实例
     */
    @Bean
    public GroupedOpenApi userApiGroup() {
        return GroupedOpenApi.builder()
                .group("user-api")
                .displayName("用户端 API")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /**
     * 自定义 OpenAPI 信息。
     * 配置文档标题、版本及全局 Authorization 请求头。
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("小兔鲜儿 API")
                        .version("1.0.0")
                        .description("小兔鲜儿 B2C 电商平台接口文档"))
                // 全局安全认证
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
