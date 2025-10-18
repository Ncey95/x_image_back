package com.ncey95.x_image_back.exception;

import lombok.Data;
import lombok.Getter;
import org.aspectj.bridge.IMessage;


// 自定义异常类，用于封装异常信息
@Getter
public class Exception extends RuntimeException {
    private final int errorCode;

    public Exception(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public Exception(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public Exception(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
    }
}
