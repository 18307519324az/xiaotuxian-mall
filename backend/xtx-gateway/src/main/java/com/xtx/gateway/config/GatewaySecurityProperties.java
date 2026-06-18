package com.xtx.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关安全配置属性
 * 从配置文件读取 "xtx.security" 前缀的白名单等配置项
 */
@Data
@ConfigurationProperties(prefix = "xtx.security")
public class GatewaySecurityProperties {

    /** 白名单路径列表，匹配到的路径不需要鉴权 */
    private List<String> whitelist = new ArrayList<>();
}
