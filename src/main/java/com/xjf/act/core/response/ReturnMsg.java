package com.xjf.act.core.response;

public enum ReturnMsg {
    SUCCESS("0", "操作成功"),
    EXCEPTION1("10001", "未知异常"),
    EXCEPTION2("10002", "操作失败"),
    EXCEPTION3("10003", "上传失败"),
    EXCEPTION4("10004", "账号或密码错误"),
    EXCEPTION5("10005", "自定义异常"),
    EXCEPTION6("10006", "用户未登录"),
    //	EXCEPTION6("10006", "自定义异常"),
    ERROR("-1", "失败"),
    ERROR1("400", "请求参数错误");

    private String code;
    private String msg;

    private ReturnMsg (String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
