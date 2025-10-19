package com.ncey95.x_image_back.aop;

import com.ncey95.x_image_back.annotation.AuthCheck;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.model.enums.UserRoleEnum;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect // 切面类，用于定义通知和切入点
@Component // 组件类，用于被 Spring 容器管理
public class AuthInterceptor {

    @Resource // 自动注入用户服务
    private UserService userService; // 用户服务

    @Around("@annotation(authCheck)") // 环绕通知，在方法执行前后进行拦截
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole(); // 必须有某个角色，默认值为空字符串
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes(); // 获取当前请求的属性
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest(); // 获取当前请求的请求对象

        User loginUser = userService.getLoginUser(request); // 获取当前登录用户
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole); // 获取必须的角色枚举

        if (mustRoleEnum == null) {

            return joinPoint.proceed();
        }// 如果必须的角色枚举为空，则直接放行

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());// 获取登录用户角色枚举

        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }// 如果登录用户角色枚举为空，则抛出无权限异常


        // 如果必须的角色枚举为管理员，且登录用户角色枚举不是管理员，则抛出无权限异常
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return joinPoint.proceed();
    }
}
