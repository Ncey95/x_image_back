package com.ncey95.x_image_back.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //SUCCESS( code: 0,"ok")，message:no usages
    //PARAMS_ERROR( code: 40000message:"请求参数错误")，no usagesNOT_LOGIN_ERROR( code: 40100,message:"未登录")，no usagesNO_AUTH_ERROR( code: 40101,message:"无权限")，no usages
    //NOT_FOUND_ERROR( code: 40400,message:"请求数据不存在")，no usagesFORBIDDEN_ERROR( code: 40300,message:"禁止访问")，no usages
    //SYSTEM_ERROR( code: 50000,message:"系统内部异常")，no usagesOPERATION_ERROR( code: 50001,message:"操作失败");no usages
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
