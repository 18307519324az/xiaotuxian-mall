package com.xtx.common.web.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前端控制器注解。
 * 组合了 @RestController，用于标记面向前端（APP / H5 / PC）的控制器类，
 * 方便统一识别和切面处理。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface FrontController {
}
