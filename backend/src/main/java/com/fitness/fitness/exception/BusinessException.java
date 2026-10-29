package com.fitness.fitness.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {
    private Integer code;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
    }

    // 未登录异常快捷构造
    public static BusinessException unLogin() {
        return new BusinessException(401, "登录失效，请重新登录");
    }
}