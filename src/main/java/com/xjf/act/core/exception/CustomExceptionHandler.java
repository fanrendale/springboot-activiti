package com.xjf.act.core.exception;

import com.xjf.act.core.response.ReturnMsg;
import com.xjf.act.core.response.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 *
 * @Author: xjf
 * @Since: 2019/12/21 14:50
 */
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    /**
     * 捕获全局异常，处理所有不可知的异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception e){
        log.error("出现全局异常错误:{}", e.getMessage());

        e.printStackTrace();

        return e.getMessage();
    }

    /**
     * 捕获自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleMyException(BusinessException e){
        log.error("出现自定义异常错误:{}", e.getMessage());

        e.printStackTrace();

        return ReturnResult.error(e.getMessage(),ReturnMsg.EXCEPTION5.getCode(), e.getMessage());

    }

    /**
     * 捕获流程参数异常
     * @param e
     * @return
     */
    @ExceptionHandler(ProcessParamException.class)
    public Object handleProcessParamException(ProcessParamException e){
        log.error("流程参数异常:{}", e.getMessage(),e);
        e.printStackTrace();

        return ReturnResult.error(e.getMessage(), ReturnMsg.EXCEPTION5.getCode(), e.getMessage());

    }
}
