package com.onik.flowticket.common;

/**
 * Redis 通道常量。
 */
public final class RedisChannelConstant {
    /**
     * 站内消息实时通知通道，所有应用实例都订阅这一条通道。
     */
    public static final String MESSAGE_NOTICE_CHANNEL = "flowticket:message:notice";
}
