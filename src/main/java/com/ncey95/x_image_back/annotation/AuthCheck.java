package com.ncey95.x_image_back.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查
 *
 * @author Ncey95
 * @since 2025-10-19
 * 用于检查用户是否有某个角色权限
 */
@Target(ElementType.METHOD) // 用于方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
public @interface AuthCheck {
    // 必须有某个角色
    String mustRole() default ""; // 默认值为空字符串
}
