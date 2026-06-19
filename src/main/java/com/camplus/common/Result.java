package com.camplus.common;

/**
 * 统一响应包装类
 *
 * @param <T> data 字段的数据类型
 */
public class Result<T> {

    private boolean success;
    private String msg;
    private T data;

    public Result() {}

    public Result(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(true, msg, data);
    }

    public static <T> Result<T> ok(String msg) {
        return new Result<>(true, msg, null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(false, msg, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
