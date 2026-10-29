package com.fitness.fitness.vo;

import lombok.Data;

/**
 * 全局统一响应封装类
 * @param <T> 返回数据泛型
 */
@Data
public class Result<T> {
    // 响应码：200成功，500业务异常，401未登录
    private Integer code;
    // 响应提示信息
    private String msg;
    // 返回业务数据
    private T data;

    // ---------------------- 成功静态方法 ----------------------
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // ---------------------- 失败静态方法 ----------------------
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 未登录专用返回
    public static <T> Result<T> unLogin() {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMsg("登录失效，请重新登录");
        return result;
    }
}