package com.onik.flowticket.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketCommentVo {
    private Long id;
    private Long ticketId;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private Long parentId;
    private String content;
    private LocalDateTime createTime;
}
