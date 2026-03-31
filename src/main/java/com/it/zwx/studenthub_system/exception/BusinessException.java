package com.it.zwx.studenthub_system.exception;

/**
 * 业务异常类：专门用于抛出业务相关的错误（如参数错误、账号异常等）
 */
public class BusinessException extends RuntimeException {
    // 可添加错误码字段（可选，用于前端更精细的错误处理）
    private Integer code;

    // 构造方法：接收错误信息
    public BusinessException(String message) {
        super(message);
    }

    // 带错误码的构造方法（可选）
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // getter（可选）
    public Integer getCode() {
        return code;
    }
}