package com.example.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码枚举，供 Result 及各微服务使用。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),
    FAIL(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_INVALID(400, "参数错误"),
    DUPLICATE_KEY(409, "数据已存在");

    private final int code;
    private final String message;
}
