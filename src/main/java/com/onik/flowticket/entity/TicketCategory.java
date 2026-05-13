package com.onik.flowticket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketCategory {
    private Long id;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Integer sort;
    private Long status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long deleted;
}
