package com.xjf.act.core.response;

public class ReturnResult {
    private Object data;
    private String code;
    private String msg;

    private ReturnResult( Object data, String code, String msg) {

        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    private ReturnResult(Object data) {
        this.data = data;
        this.code = ReturnMsg.SUCCESS.getCode();
        this.msg = ReturnMsg.SUCCESS.getMsg();
    }



    private ReturnResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private ReturnResult() {
        this.code = ReturnMsg.ERROR.getCode();
        this.msg = ReturnMsg.ERROR.getMsg();
    }

    /***
     * 输入参数:
     *
     * @param data
     * @param code
     * @param msg
     * @return 返回成功，包含code、msg、data
     */
    public static ReturnResult success(Object data, String code, String msg) {
        return new ReturnResult( data, code, msg);
    }



    /***
     * 输入参数：
     *
     * @param data
     * @return 返回成功，包含code、msg、data
     */
    public static ReturnResult success(Object data) {
        return new ReturnResult(data);
    }

    /***
     * 无输入参数 返回成功，包含code、msg
     *
     * @return
     */
    public static ReturnResult success() {
        return new ReturnResult("");
    }

    /***
     * 无输入参数 返回失败，包含code、msg
     *
     * @return
     */
    public static ReturnResult error() {
        return new ReturnResult();
    }

    public static ReturnResult error(String msg){
        return new ReturnResult(ReturnMsg.EXCEPTION2.getCode(),msg);
    }

    public static ReturnResult error(String code, String msg) {
        return new ReturnResult(code, msg);
    }

    public static ReturnResult error(Object data,String code,String msg){
        return new ReturnResult(data, code, msg);
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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
