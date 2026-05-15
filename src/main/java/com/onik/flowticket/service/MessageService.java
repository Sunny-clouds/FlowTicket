package com.onik.flowticket.service;

import com.onik.flowticket.entity.Message;

import java.util.List;

public interface MessageService {
    /**
     * 创建站内消息，并触发实时提醒。
     */
    void create(Long receiverId, Long senderId, Long ticketId, String messageType, String title, String content);

    /**
     * 查询当前登录用户收到的消息。
     */
    List<Message> listCurrentUserMessages();

    /**
     * 查询当前登录用户未读消息数量。
     */
    Long countCurrentUserUnread();

    /**
     * 标记当前用户的一条消息为已读。
     */
    void markRead(Long id);

    /**
     * 标记当前用户的全部消息为已读。
     */
    void markAllRead();
}
