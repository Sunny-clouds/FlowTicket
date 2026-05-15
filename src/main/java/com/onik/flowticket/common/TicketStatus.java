package com.onik.flowticket.common;

/**
 *
 * 工单状态常量。
 */
public final class TicketStatus {
    // 工单状态值与数据库 ticket.status 字段保持一致，避免在业务代码里散落魔法数字。
    public static final Integer PENDING = 1;
    public static final Integer PROCESSING = 2;
    public static final Integer WAIT_CONFIRM = 3;
    public static final Integer COMPLETED = 4;
    public static final Integer REJECTED = 5;

    /**
     * 将数据库里的状态数字转换成前端可直接展示的中文名称。
     */
    public static String nameOf(Integer status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case 1 -> {return "待受理";}
            case 2 -> {return "处理中";}
            case 3 -> {return "待确认";}
            case 4 -> {return "已完成";}
            case 5 -> {return "已驳回";}
            default -> {return "未知";}
        }
    }

    /**
     * 终态工单不允许继续处理、回复、分配或改优先级。
     */
    public static boolean terminal(Integer status) {
        return COMPLETED.equals(status) || REJECTED.equals(status);
    }
}
