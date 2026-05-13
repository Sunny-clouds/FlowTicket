package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketAttachment {
    private Long id;
    private Long ticketId;
    private Long uploaderId;
    private String originalName;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createTime;
    private Integer deleted;
}
