package com.Mike12138210.SmartNote.advice;

import com.Mike12138210.SmartNote.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 标记为全局异常处理，并返回JSON
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e){
        System.out.println("业务异常：" + e.getMessage());
        return Result.error(500,e.getMessage()); // e.getMessage为throw时传入的字符串
    }
}