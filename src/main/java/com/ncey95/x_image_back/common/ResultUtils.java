package com.ncey95.x_image_back.common;

import com.ncey95.x_image_back.exception.ErrorCode;

// 结果工具类，用于封装API返回结果
public class ResultUtils {


    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }


    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }


    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }


    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
