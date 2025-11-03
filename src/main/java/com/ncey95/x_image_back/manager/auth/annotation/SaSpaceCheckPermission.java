package com.ncey95.x_image_back.manager.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ncey95.x_image_back.manager.auth.StpKit;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SaCheckPermission(type = StpKit.SPACE_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SaSpaceCheckPermission {

    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};
}
