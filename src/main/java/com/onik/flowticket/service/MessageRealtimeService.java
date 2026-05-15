package com.onik.flowticket.service;

import com.onik.flowticket.vo.MessageNoticeVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MessageRealtimeService {
    /**
     * 为当前用户建立 SSE 实时消息连接。
     */
    SseEmitter connect();

    /**
     * 将消息发布到 Redis 通道。
     */
    void publish(MessageNoticeVo notice);

    /**
     * Redis 订阅回调，收到消息后推送给本机对应用户连接。
     */
    void handleRedisMessage(String noticeJson);
}
