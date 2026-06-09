package com.devcopilot.common.exception;

public enum ErrorCode {
    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源状态冲突"),
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
