package com.onik.flowticket.service.impl;

import com.onik.flowticket.entity.Message;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.mapper.MessageMapper;
import com.onik.flowticket.service.MessageRealtimeService;
import com.onik.flowticket.service.MessageService;
import com.onik.flowticket.utils.SecurityUtils;
import com.onik.flowticket.vo.MessageNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageRealtimeService messageRealtimeService;
    @Autowired
    private SecurityUtils securityUtils;

    @Override
    @Transactional
    public void create(Long receiverId, Long senderId, Long ticketId, String messageType, String title, String content) {
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setSenderId(senderId);
        message.setTicketId(ticketId);
        message.setMessageType(messageType);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 消息先入库，再等事务提交后发布提醒，避免业务回滚时前端收到无效通知。
        Long unreadCount = messageMapper.countUnread(receiverId);
        publishAfterCommit(MessageNoticeVo.from(message, unreadCount));
    }

    @Override
    public List<Message> listCurrentUserMessages() {
        User current = securityUtils.currentUser();
        return messageMapper.selectByReceiverId(current.getId());
    }

    @Override
    public Long countCurrentUserUnread() {
        User current = securityUtils.currentUser();
        return messageMapper.countUnread(current.getId());
    }

    @Override
    public void markRead(Long id) {
        User current = securityUtils.currentUser();
        messageMapper.markRead(id, current.getId());
    }

    @Override
    public void markAllRead() {
        User current = securityUtils.currentUser();
        messageMapper.markAllRead(current.getId());
    }

    private void publishAfterCommit(MessageNoticeVo notice) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messageRealtimeService.publish(notice);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messageRealtimeService.publish(notice);
            }
        });
    }
}
