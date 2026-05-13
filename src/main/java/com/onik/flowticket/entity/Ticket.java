package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    private Long id;
    private String ticketNo;
    private String title;
    private String content;
    private Long categoryId;
    private Long creatorId;
    private Long assigneeId;
    private Integer priority;
    private Integer status;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String handleResult;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
