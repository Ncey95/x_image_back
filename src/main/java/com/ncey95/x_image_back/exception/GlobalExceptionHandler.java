package com.ncey95.x_image_back.exception;

import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)// 业务异常处理
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("业务异常信息:{}", e.getMessage());
        return ResultUtils.error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常信息:{}",e.getMessage());
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}