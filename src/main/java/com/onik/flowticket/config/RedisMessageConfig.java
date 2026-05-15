package com.onik.flowticket.config;

import com.onik.flowticket.common.RedisChannelConstant;
import com.onik.flowticket.service.MessageRealtimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Redis 消息发布/订阅配置。
 */
@Configuration
public class RedisMessageConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key 和 Value 都使用字符串，发布订阅时直接传 JSON 文本，避免类型反序列化不一致。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       MessageRealtimeService messageRealtimeService) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // 订阅 Redis 通道，收到 JSON 字符串后交给业务服务解析并推送给本机 SSE 连接。
        container.addMessageListener((message, pattern) -> messageRealtimeService.handleRedisMessage(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(RedisChannelConstant.MESSAGE_NOTICE_CHANNEL));
        return container;
    }
}
