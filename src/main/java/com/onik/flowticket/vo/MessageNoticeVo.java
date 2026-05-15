package com.onik.flowticket.vo;

import com.onik.flowticket.entity.Message;
import lombok.Data;

/**
 * 推送给前端的实时消息提醒。
 */
@Data
public class MessageNoticeVo {
    private Long id;
    private Long receiverId;
    private Long senderId;
    private Long ticketId;
    private String messageType;
    private String title;
    private String content;
    private Integer isRead;
    private Long unreadCount;
    private String createTime;

    /**
     * 从消息实体构造推送对象，并带上最新未读数。
     */
    public static MessageNoticeVo from(Message message, Long unreadCount) {
        MessageNoticeVo notice = new MessageNoticeVo();
        notice.setId(message.getId());
        notice.setReceiverId(message.getReceiverId());
        notice.setSenderId(message.getSenderId());
        notice.setTicketId(message.getTicketId());
        notice.setMessageType(message.getMessageType());
        notice.setTitle(message.getTitle());
        notice.setContent(message.getContent());
        notice.setIsRead(message.getIsRead());
        notice.setUnreadCount(unreadCount);
        notice.setCreateTime(message.getCreateTime() == null ? null : message.getCreateTime().toString());
        return notice;
    }
}
