package com.onik.flowticket.common;

/**
 * 站内消息类型、标题和内容常量。
 */
public final class MessageConstant {
    public static final String TYPE_TICKET_ASSIGN = "TICKET_ASSIGN";
    public static final String TYPE_TICKET_PROCESS = "TICKET_PROCESS";
    public static final String TYPE_TICKET_CONFIRM = "TICKET_CONFIRM";
    public static final String TYPE_TICKET_REJECT = "TICKET_REJECT";
    public static final String TYPE_TICKET_CLOSE = "TICKET_CLOSE";
    public static final String TYPE_TICKET_URGE = "TICKET_URGE";

    public static final String TITLE_TICKET_ASSIGN = "你有新的工单待处理";
    public static final String TITLE_TICKET_PROCESS = "你的工单正在处理";
    public static final String TITLE_TICKET_CONFIRM = "工单待确认";
    public static final String TITLE_TICKET_REJECT = "工单已驳回";
    public static final String TITLE_TICKET_CLOSE = "工单已完成";
    public static final String TITLE_TICKET_URGE = "用户催促工单";

    public static final String CONTENT_TICKET_ASSIGN = "管理员已将工单分配给你，请及时处理。";
    public static final String CONTENT_TICKET_PROCESS = "你的工单已由客服受理。";
    public static final String CONTENT_TICKET_CONFIRM = "客服已处理完成，请确认工单是否完成。";
    public static final String CONTENT_TICKET_REJECT = "你的工单不符合处理条件，已被驳回。";
    public static final String CONTENT_TICKET_CLOSE = "工单状态已更新为已完成。";
    public static final String CONTENT_TICKET_URGE_PENDING = "用户催促待受理工单，请尽快分配处理。";
    public static final String CONTENT_TICKET_URGE_PROCESSING = "用户催促处理中工单，请尽快跟进处理。";
}
