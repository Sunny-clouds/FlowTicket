package com.onik.flowticket.common;

/**
 *
 * 错误信息常量类
 */
public final class ErrorMessage {

    public static final String PARAM_EMPTY = "参数不能为空";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USERNAME_OR_PASSWORD_ERROR = "用户名或密码错误";
    public static final String UNAUTHORIZED = "请先登录";
    public static final String TOKEN_INVALID = "登录状态已失效，请重新登录";
    public static final String ACCESS_DENIED = "没有权限访问该资源";
    public static final String SAVE_USER_FAILED = "新增用户失败";
    public static final String UPDATE_USER_FAILED = "修改用户失败";
    public static final String DELETE_USER_FAILED = "删除用户失败";
    public static final String USERNAME_EXISTS = "用户名已存在";
    public static final String USER_DISABLED = "账号已被禁用";
    public static final String LOGIN_USER_NOT_FOUND = "登录用户不存在";
    public static final String ADMIN_REQUIRED = "需要管理员权限";
    public static final String HANDLER_REQUIRED = "需要客服权限";
    public static final String CATEGORY_NAME_EMPTY = "分类名称不能为空";
    public static final String CATEGORY_ID_EMPTY = "分类ID不能为空";
    public static final String TICKET_LIST_ACCESS_DENIED = "无权查看工单列表";
    public static final String TICKET_NOT_FOUND = "工单不存在";
    public static final String TICKET_CREATE_USER_ONLY = "只有普通用户可以提交工单";
    public static final String TICKET_TITLE_CONTENT_EMPTY = "工单标题和内容不能为空";
    public static final String TICKET_ASSIGN_STATUS_INVALID = "当前状态不能分配工单";
    public static final String TICKET_ASSIGNEE_EMPTY = "处理人不能为空";
    public static final String TICKET_ASSIGNEE_INVALID = "请选择客服处理人员";
    public static final String TICKET_PRIORITY_EMPTY = "优先级不能为空";
    public static final String TICKET_COMMENT_EMPTY = "回复内容不能为空";
    public static final String TICKET_COMPLETE_STATUS_INVALID = "只有处理中的工单可以提交确认";
    public static final String TICKET_HANDLE_RESULT_EMPTY = "处理结果不能为空";
    public static final String TICKET_CLOSE_OWN_WAIT_CONFIRM_ONLY = "只能确认关闭自己的待确认工单";
    public static final String TICKET_HANDLER_CLOSE_PROCESSING_ONLY = "客服只能关闭处理中的工单";
    public static final String TICKET_CLOSE_ACCESS_DENIED = "无权关闭工单";
    public static final String TICKET_REJECT_PENDING_ONLY = "只有待受理工单可以驳回";
    public static final String TICKET_VIEW_OWN_ONLY = "只能查看自己的工单";
    public static final String TICKET_VIEW_ASSIGNED_ONLY = "只能查看分配给自己的工单";
    public static final String TICKET_COMMENT_ACCESS_DENIED = "无权回复该工单";
    public static final String TICKET_HANDLE_ASSIGNED_ONLY = "只能处理分配给自己的工单";
    public static final String TICKET_TERMINAL_CANNOT_MODIFY = "已关闭或已驳回的工单不能继续修改";
}
