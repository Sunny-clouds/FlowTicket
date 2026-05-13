package com.onik.flowticket.dto;

import lombok.Data;

@Data
public class TicketCommentDto {
    private Long parentId;
    private String content;
}
