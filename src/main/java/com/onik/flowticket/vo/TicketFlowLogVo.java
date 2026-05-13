package com.onik.flowticket.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketFlowLogVo {
    private Long id;
    private Long ticketId;
    private Long operatorId;
    private String operatorName;
    private String operationType;
    private String operationDesc;
    private Integer beforeStatus;
    private String beforeStatusName;
    private Integer afterStatus;
    private String afterStatusName;
    private Long beforeAssigneeId;
    private Long afterAssigneeId;
    private LocalDateTime createTime;
}
