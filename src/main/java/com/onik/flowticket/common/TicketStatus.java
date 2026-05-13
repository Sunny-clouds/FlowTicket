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
    public static final Integer CLOSED = 5;
    public static final Integer REJECTED = 6;

    /**
     * 将数据库里的状态数字转换成前端可直接展示的中文名称。
     */
    public static String nameOf(Integer status) {
        if (PENDING.equals(status)) {
            return "待受理";
        }
        if (PROCESSING.equals(status)) {
            return "处理中";
        }
        if (WAIT_CONFIRM.equals(status)) {
            return "待确认";
        }
        if (COMPLETED.equals(status)) {
            return "已完成";
        }
        if (CLOSED.equals(status)) {
            return "已关闭";
        }
        if (REJECTED.equals(status)) {
            return "已驳回";
        }
        return "未知";
    }

    /**
     * 终态工单不允许继续处理、回复、分配或改优先级。
     */
    public static boolean terminal(Integer status) {
        return CLOSED.equals(status) || REJECTED.equals(status);
    }
}
