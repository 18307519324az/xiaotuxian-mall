package com.xtx.common.web.config;

import com.xtx.common.web.interceptor.UserContextInterceptor;
import com.xtx.common.web.resolver.XUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置。
 * 注册拦截器、参数解析器及跨域配置，统一管理 Web 层行为。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册用户上下文拦截器，拦截所有请求路径。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**");
    }

    /**
     * 注册 @XUserId 参数解析器。
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new XUserIdArgumentResolver());
    }

    /**
     * 配置跨域访问。
     * 允许所有来源、方法和请求头，并允许携带凭证。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 配置内容协商，禁用 URL 参数方式的内容协商。
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(false);
    }
}
