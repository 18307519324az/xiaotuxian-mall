package com.xtx.common.mybatisflex.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 全局配置。
 * MyBatis-Flex 的 @Column(onInsertValue = "NOW()", onUpdateValue = "NOW()")
 * 注解即可满足 createTime / updateTime 字段的自动填充需求，无需额外配置。
 */
@Configuration
public class MybatisFlexConfig {

}
