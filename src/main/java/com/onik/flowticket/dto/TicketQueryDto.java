package com.onik.flowticket.dto;

import lombok.Data;

@Data
public class TicketQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Integer status;
    private Integer priority;
    private Long categoryId;
    private Long creatorId;
    private Long assigneeId;
    private String keyword;
}
