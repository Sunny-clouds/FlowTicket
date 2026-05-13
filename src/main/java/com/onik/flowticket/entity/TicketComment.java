package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketComment {
    private Long id;
    private Long ticketId;
    private Long userId;
    private Long parentId;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
