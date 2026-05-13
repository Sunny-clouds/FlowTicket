package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long id;
    private Long receiverId;
    private Long senderId;
    private Long ticketId;
    private String messageType;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private Integer deleted;
}
