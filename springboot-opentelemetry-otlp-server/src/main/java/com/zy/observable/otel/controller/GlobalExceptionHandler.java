package com.zy.observable.otel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 2021/11/10
 * <p>
 * 全局异常处理器
 */
@RestControllerAdvice //对Controller增强,并返回json格式字符串
public class GlobalExceptionHandler {
    public static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获Exception异常,并自定义返回数据
     */
    @ExceptionHandler(Exception.class)
    public String exception(Exception e, HttpServletRequest request) {
        log.error("request error!! method:{} uri:{}", request.getMethod(), request.getRequestURI());
        log.error(getExceptionDetail(e));
        return request.getMethod() + " " + request.getRequestURI() + " " + e.getMessage();
    }

    /**
     * Assert异常
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(IllegalArgumentException e,HttpServletRequest request) {
        log.error("request error!! method:{} uri:{}", request.getMethod(), request.getRequestURI());
        String message = getExceptionDetail(e);
        String message1 = e.getMessage();
        return e.getMessage();
    }



    /**
     * 获取代码报错详细位置信息
     */
    public String getExceptionDetail(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(e.getClass()).append(System.getProperty("line.separator"));
        stringBuilder.append(e.getLocalizedMessage()).append(System.getProperty("line.separator"));
        StackTraceElement[] arr = e.getStackTrace();
        for (StackTraceElement stackTraceElement : arr) {
            stringBuilder.append(stackTraceElement.toString()).append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();
    }
}