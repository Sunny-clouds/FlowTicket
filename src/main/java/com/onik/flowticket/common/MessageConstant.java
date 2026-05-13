package com.onik.flowticket.common;

/**
 * 站内消息类型、标题和内容常量。
 */
public final class MessageConstant {
    public static final String TYPE_TICKET_ASSIGN = "TICKET_ASSIGN";
    public static final String TYPE_TICKET_PROCESS = "TICKET_PROCESS";
    public static final String TYPE_TICKET_CONFIRM = "TICKET_CONFIRM";
    public static final String TYPE_TICKET_REJECT = "TICKET_REJECT";

    public static final String TITLE_TICKET_ASSIGN = "你有新的工单待处理";
    public static final String TITLE_TICKET_PROCESS = "你的工单正在处理";
    public static final String TITLE_TICKET_CONFIRM = "工单待确认";
    public static final String TITLE_TICKET_REJECT = "工单已驳回";

    public static final String CONTENT_TICKET_ASSIGN = "管理员已将工单分配给你，请及时处理。";
    public static final String CONTENT_TICKET_PROCESS = "你的工单已由客服受理。";
    public static final String CONTENT_TICKET_CONFIRM = "客服已处理完成，请确认是否关闭工单。";
    public static final String CONTENT_TICKET_REJECT = "你的工单不符合处理条件，已被驳回。";
}
