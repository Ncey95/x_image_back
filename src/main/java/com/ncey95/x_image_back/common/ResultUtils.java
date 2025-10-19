package com.ncey95.x_image_back.common;

import com.ncey95.x_image_back.exception.ErrorCode;

// 结果工具类，用于封装API返回结果
public class ResultUtils {


    public static <T> BaseResponse<T> success(T data) {

        return new BaseResponse<>(0, data, "ok");
    }// 成功，返回数据


    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }// 失败，返回错误码


    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }// 失败,返回错误码和错误信息


    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }// 失败,返回错误码和错误信息
}
