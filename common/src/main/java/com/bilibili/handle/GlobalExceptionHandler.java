package com.bilibili.handle;

import com.bilibili.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestControllerAdvice
@ConditionalOnProperty(prefix = "bilibili", name = "exception-handler", havingValue = "true")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception exception, HttpServletResponse httpServletResponse) {
        //获取绑定异常的结果
        httpServletResponse.setStatus(600);
        exception.printStackTrace();
        log.error("", exception);
        return Result.error("有问题，程序寄了," + exception.getMessage());
    }
}
