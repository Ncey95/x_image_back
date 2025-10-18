package com.ncey95.x_image_back.common;

import com.ncey95.x_image_back.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;
// 通用返回类，用于封装API返回结果
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
