package com.onik.flowticket.common;

/**
 * Redis Key 常量。
 */
public final class RedisKeyConstant {
    /**
     * 用户登录 token，完整 key 为 flowticket:user:token:{userId}。
     */
    public static final String USER_TOKEN_PREFIX = "flowticket:user:token:";
}
