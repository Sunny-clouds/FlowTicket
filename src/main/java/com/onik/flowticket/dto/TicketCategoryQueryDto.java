package com.onik.flowticket.dto;

import lombok.Data;

@Data
public class TicketCategoryQueryDto {
    /**
     * 保留给工单创建页面使用，只返回启用状态的分类。
     */
    private Boolean onlyEnabled = false;

    /**
     * 分类管理列表的状态筛选条件，1 表示启用，0 表示停用。
     */
    private Integer status;
}
