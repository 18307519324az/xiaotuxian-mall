package com.xtx.common.web.resolver;

import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.web.context.UserContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link XUserId} 注解的参数解析器。
 * 当控制器方法参数标注了 @XUserId 且类型为 Long 时，
 * 自动从 {@link UserContextHolder} 中注入当前登录用户 ID。
 */
public class XUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(XUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return UserContextHolder.getUserId();
    }
}
