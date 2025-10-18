package com.ncey95.x_image_back.exception;

public class ThrowUtils {
    // 断言条件为true，否则抛出异常
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new Exception(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new Exception(errorCode, message));
    }
}
