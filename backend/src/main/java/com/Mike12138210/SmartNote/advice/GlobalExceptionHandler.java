package com.Mike12138210.SmartNote.advice;

import com.Mike12138210.SmartNote.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 标记为全局异常处理，并返回JSON
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e){
        log.error("业务异常：",e);
        return Result.error(500,e.getMessage()); // e.getMessage为throw时传入的字符串
    }
}