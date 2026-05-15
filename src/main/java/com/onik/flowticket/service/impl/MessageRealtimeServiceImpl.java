package com.onik.flowticket.service.impl;

import com.onik.flowticket.common.RedisChannelConstant;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.service.MessageRealtimeService;
import com.onik.flowticket.utils.SecurityUtils;
import com.onik.flowticket.vo.MessageNoticeVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MessageRealtimeServiceImpl implements MessageRealtimeService {
    /**
     * SSE 默认连接保持 30 分钟，前端断线后会自动重连。
     */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 同一个用户可能打开多个浏览器页签，因此每个用户维护一组连接。
     */
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SseEmitter connect() {
        User current = securityUtils.currentUser();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(current.getId(), key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(current.getId(), emitter));
        emitter.onTimeout(() -> removeEmitter(current.getId(), emitter));
        emitter.onError(error -> removeEmitter(current.getId(), emitter));

        try {
            // 建连后先发一个握手事件，避免代理或浏览器认为连接无内容。
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException exception) {
            removeEmitter(current.getId(), emitter);
        }
        return emitter;
    }

    @Override
    public void publish(MessageNoticeVo notice) {
        try {
            redisTemplate.convertAndSend(RedisChannelConstant.MESSAGE_NOTICE_CHANNEL, objectMapper.writeValueAsString(notice));
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("消息提醒序列化失败", exception);
        }
    }

    @Override
    public void handleRedisMessage(String noticeJson) {
        MessageNoticeVo notice;
        try {
            notice = objectMapper.readValue(noticeJson, MessageNoticeVo.class);
        } catch (JsonProcessingException exception) {
            return;
        }
        if (notice == null || notice.getReceiverId() == null) {
            return;
        }
        List<SseEmitter> userEmitters = emitters.get(notice.getReceiverId());
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("message").data(notice));
            } catch (IOException exception) {
                removeEmitter(notice.getReceiverId(), emitter);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
