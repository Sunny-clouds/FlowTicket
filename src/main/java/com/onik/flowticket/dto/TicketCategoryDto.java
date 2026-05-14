package com.onik.flowticket.dto;

import lombok.Data;

@Data
public class TicketCategoryDto {
    private Long id;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Integer sort;
    private Long status;
}
