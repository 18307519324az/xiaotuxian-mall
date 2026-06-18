package com.xtx.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从请求头中提取当前登录用户 ID 的参数注解。
 * 标注在控制器方法参数上，通过参数解析器自动注入当前用户 ID。
 * <pre>
 *     &#064;GetMapping("/info")
 *     public Result info(&#064;XUserId Long userId) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface XUserId {
}
