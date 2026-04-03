package com.example.common.constant;

/**
 * 微服务公共常量。
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /** 请求头：链路追踪 ID */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    /** 请求头：租户 ID（若做多租户） */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
}
