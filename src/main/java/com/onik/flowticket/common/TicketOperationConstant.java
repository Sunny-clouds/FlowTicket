package com.onik.flowticket.common;

/**
 * 工单流转日志操作类型和描述常量。
 */
public final class TicketOperationConstant {
    public static final String CREATE = "CREATE";
    public static final String ASSIGN = "ASSIGN";
    public static final String PRIORITY = "PRIORITY";
    public static final String HANDLER_REPLY = "HANDLER_REPLY";
    public static final String USER_REPLY = "USER_REPLY";
    public static final String COMPLETE = "COMPLETE";
    public static final String CLOSE = "CLOSE";
    public static final String REJECT = "REJECT";

    public static final String DESC_CREATE = "用户创建工单";
    public static final String DESC_ASSIGN = "管理员将工单分配给客服";
    public static final String DESC_PRIORITY = "管理员修改工单优先级";
    public static final String DESC_REPLY = "新增工单回复";
    public static final String DESC_COMPLETE = "客服处理完成，等待用户确认";
    public static final String DESC_CLOSE = "工单关闭";
    public static final String DESC_REJECT = "管理员驳回工单";
}
