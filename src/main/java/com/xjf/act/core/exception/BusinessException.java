package com.xjf.act.core.exception;

/**
 * 自定义异常:业务异常
 *
 * @Author: xjf
 * @Since: 2019/12/21 14:49
 */
public class BusinessException extends RuntimeException {
    /**
     * 存放具体的异常信息
     */
    private String message;

    public BusinessException(String message) {

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
