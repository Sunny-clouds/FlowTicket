package com.onik.flowticket.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketVo {
    private Long id;
    private String ticketNo;
    private String title;
    private String content;
    private Long categoryId;
    private String categoryName;
    private Long creatorId;
    private String creatorName;
    private Long assigneeId;
    private String assigneeName;
    private Integer priority;
    private Integer status;
    private String statusName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String handleResult;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean canUrge;
    private String urgeMessage;
}
