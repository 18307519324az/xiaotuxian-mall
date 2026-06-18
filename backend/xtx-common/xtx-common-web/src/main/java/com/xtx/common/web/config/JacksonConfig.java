package com.xtx.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置。
 * 配置 Long 类型序列化为字符串（解决前端 JavaScript 精度丢失），
 * 配置 LocalDateTime 全局日期格式，仅在 Web 层生效，不影响 Feign 客户端。
 */
@Configuration
public class JacksonConfig {

    /**
     * Web 层专用的 ObjectMapper。
     * 标注 @Primary，确保注入到 Web 相关的消息转换器中。
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Long 类型序列化为字符串，防止前端 JS 精度丢失
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        mapper.registerModule(longModule);

        // 配置 Java 8 日期时间序列化格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        mapper.registerModule(javaTimeModule);

        // 禁用空对象序列化失败特性
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 禁止将日期序列化为时间戳，使用字符串格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
