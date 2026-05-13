package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFlowLog {
    private Long id;
    private Long ticketId;
    private Long operatorId;
    private String operationType;
    private String operationDesc;
    private Integer beforeStatus;
    private Integer afterStatus;
    private Long beforeAssigneeId;
    private Long afterAssigneeId;
    private LocalDateTime createTime;
}
