package com.onik.flowticket.dto;

import lombok.Data;

@Data
public class TicketCreateDto {
    private String title;
    private String content;
    private Long categoryId;
    private Integer priority;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
}
