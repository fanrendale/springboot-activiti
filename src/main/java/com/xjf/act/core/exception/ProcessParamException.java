package com.xjf.act.core.exception;

/**
 * 流程的参数异常
 *
 * @Author: xjf
 * @Since: 2019/12/18 15:13
 */
public class ProcessParamException extends RuntimeException {

    /**
     * 存放具体的异常信息
     */
    private String message;

    public ProcessParamException(String message) {

        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
