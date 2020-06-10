package com.xiezy.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@ResponseBody
public class RPCExceptionHandler {
    @ExceptionHandler(value = RPCException.class) //该注解声明异常处理方法
    public Object exceptionHandler(HttpServletRequest request, Exception e){
        return e.getMessage();
    }
}
