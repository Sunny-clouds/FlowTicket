package com.onik.flowticket.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.onik.flowticket.common.*;
import com.onik.flowticket.dto.*;
import com.onik.flowticket.entity.Ticket;
import com.onik.flowticket.entity.TicketComment;
import com.onik.flowticket.entity.TicketFlowLog;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.mapper.*;
import com.onik.flowticket.service.MessageService;
import com.onik.flowticket.service.TicketService;
import com.onik.flowticket.utils.SecurityUtils;
import com.onik.flowticket.vo.TicketCommentVo;
import com.onik.flowticket.vo.TicketFlowLogVo;
import com.onik.flowticket.vo.TicketVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;
    @Autowired
    private TicketCommentMapper ticketCommentMapper;
    @Autowired
    private TicketFlowLogMapper ticketFlowLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private MessageService messageService;

    /**
     * 分页查询工单列表，并根据当前用户角色限制可见范围。
     */
    @Override
    public PageResult<TicketVo> page(TicketQueryDto queryDto) {
        User current = securityUtils.currentUser();
        TicketQueryDto query = queryDto == null ? new TicketQueryDto() : queryDto;
        // 保护分页参数，避免前端未传或传负数时影响 PageHelper。
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(BusinessConstant.DEFAULT_PAGE_NUM);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(BusinessConstant.DEFAULT_PAGE_SIZE);
        }
        // 列表范围由当前用户角色决定：用户看自己创建的，客服看分配给自己的，管理员看全部。
        if (RoleConstant.USER.equals(current.getRole())) {
            query.setCreatorId(current.getId());
        } else if (RoleConstant.HANDLER.equals(current.getRole())) {
            query.setAssigneeId(current.getId());
        } else if (!RoleConstant.ADMIN.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.TICKET_LIST_ACCESS_DENIED);
        }
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<TicketVo> records = ticketMapper.selectPage(query);
        records.forEach(this::fillStatusName);
        PageInfo<TicketVo> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 查询工单详情，返回前会校验当前用户是否有查看权限。
     */
    @Override
    public TicketVo detail(Long id) {
        TicketVo ticket = ticketMapper.selectVoById(id);
        if (ticket == null) {
            throw new RuntimeException(ErrorMessage.TICKET_NOT_FOUND);
        }
        // 详情接口也要做权限控制，避免用户通过猜 id 查看别人的工单。
        ensureReadable(ticket);
        fillStatusName(ticket);
        fillUrgeInfo(ticket);
        return ticket;
    }

    /**
     * 普通用户创建新工单，生成工单编号并写入创建日志。
     */
    @Override
    @Transactional
    public void create(TicketCreateDto createDto) {
        User current = securityUtils.currentUser();
        // 工单由普通用户发起，管理员和客服只负责后续管理/处理。
        if (!RoleConstant.USER.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.TICKET_CREATE_USER_ONLY);
        }
        if (createDto == null || !StringUtils.hasText(createDto.getTitle()) || !StringUtils.hasText(createDto.getContent())) {
            throw new RuntimeException(ErrorMessage.TICKET_TITLE_CONTENT_EMPTY);
        }
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(createDto, ticket);
        // 工单号按当天日期递增生成，例如 FT202605130001。
        ticket.setTicketNo(nextTicketNo());
        ticket.setCreatorId(current.getId());
        ticket.setPriority(createDto.getPriority() == null ? BusinessConstant.DEFAULT_TICKET_PRIORITY : createDto.getPriority());
        ticket.setStatus(TicketStatus.PENDING);
        ticketMapper.insert(ticket);
        addLog(ticket.getId(), current.getId(),
                TicketOperationConstant.CREATE,
                TicketOperationConstant.DESC_CREATE, null,
                TicketStatus.PENDING, null, null);
    }

    /**
     * 管理员将工单分配给客服处理，并通知客服和创建人。
     */
    @Override
    @Transactional
    public void assign(Long id, TicketAssignDto assignDto) {
        User current = securityUtils.currentUser();
        // 分配工单会同时修改处理人和状态，所以必须放在事务里和日志/消息一起提交。
        ensureAdmin(current);
        Ticket ticket = mustTicket(id);
        ensureNotTerminal(ticket);
        if (!TicketStatus.PENDING.equals(ticket.getStatus()) && !TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            throw new RuntimeException(ErrorMessage.TICKET_ASSIGN_STATUS_INVALID);
        }
        if (assignDto == null || assignDto.getAssigneeId() == null) {
            throw new RuntimeException(ErrorMessage.TICKET_ASSIGNEE_EMPTY);
        }
        User assignee = userMapper.selectById(assignDto.getAssigneeId());
        if (assignee == null || !RoleConstant.HANDLER.equals(assignee.getRole())) {
            throw new RuntimeException(ErrorMessage.TICKET_ASSIGNEE_INVALID);
        }
        // 记录变更前的状态和处理人，用于后续流转日志追踪。
        Integer beforeStatus = ticket.getStatus();
        Long beforeAssignee = ticket.getAssigneeId();
        Ticket update = new Ticket();
        update.setId(id);
        update.setAssigneeId(assignDto.getAssigneeId());
        update.setStatus(TicketStatus.PROCESSING);
        ticketMapper.update(update);
        addLog(id, current.getId(),
                TicketOperationConstant.ASSIGN,
                TicketOperationConstant.DESC_ASSIGN, beforeStatus,
                TicketStatus.PROCESSING, beforeAssignee, assignDto.getAssigneeId());
        addMessage(assignDto.getAssigneeId(), current.getId(), id,
                MessageConstant.TYPE_TICKET_ASSIGN,
                MessageConstant.TITLE_TICKET_ASSIGN,
                MessageConstant.CONTENT_TICKET_ASSIGN);
        addMessage(ticket.getCreatorId(), current.getId(), id,
                MessageConstant.TYPE_TICKET_PROCESS,
                MessageConstant.TITLE_TICKET_PROCESS,
                MessageConstant.CONTENT_TICKET_PROCESS);
    }

    /**
     * 管理员调整工单优先级，并记录本次优先级变更日志。
     */
    @Override
    @Transactional
    public void updatePriority(Long id, PriorityUpdateDto priorityUpdateDto) {
        User current = securityUtils.currentUser();
        // 优先级只允许管理员调整，终态工单不能再修改。
        ensureAdmin(current);
        Ticket ticket = mustTicket(id);
        ensureNotTerminal(ticket);
        if (priorityUpdateDto == null || priorityUpdateDto.getPriority() == null) {
            throw new RuntimeException(ErrorMessage.TICKET_PRIORITY_EMPTY);
        }
        Ticket update = new Ticket();
        update.setId(id);
        update.setPriority(priorityUpdateDto.getPriority());
        ticketMapper.update(update);
        addLog(id, current.getId(),
                TicketOperationConstant.PRIORITY,
                TicketOperationConstant.DESC_PRIORITY,
                ticket.getStatus(), ticket.getStatus(), ticket.getAssigneeId(), ticket.getAssigneeId());
    }

    /**
     * 为工单追加回复内容，允许创建人、处理客服或管理员参与沟通。
     */
    @Override
    @Transactional
    public void addComment(Long id, TicketCommentDto commentDto) {
        User current = securityUtils.currentUser();
        Ticket ticket = mustTicket(id);
        // 回复权限由工单归属决定：创建人、当前处理客服、管理员可以回复。
        ensureCanComment(current, ticket);
        if (commentDto == null || !StringUtils.hasText(commentDto.getContent())) {
            throw new RuntimeException(ErrorMessage.TICKET_COMMENT_EMPTY);
        }
        TicketComment comment = new TicketComment();
        comment.setTicketId(id);
        comment.setUserId(current.getId());
        comment.setParentId(commentDto.getParentId() == null ? BusinessConstant.ROOT_COMMENT_PARENT_ID : commentDto.getParentId());
        comment.setContent(commentDto.getContent());
        ticketCommentMapper.insert(comment);
        String operation = RoleConstant.HANDLER.equals(current.getRole()) ? TicketOperationConstant.HANDLER_REPLY : TicketOperationConstant.USER_REPLY;
        addLog(id, current.getId(), operation,
                TicketOperationConstant.DESC_REPLY,
                ticket.getStatus(), ticket.getStatus(), ticket.getAssigneeId(), ticket.getAssigneeId());
    }

    /**
     * 客服提交处理结果，将工单流转到等待用户确认状态。
     */
    @Override
    @Transactional
    public void complete(Long id, TicketProcessDto processDto) {
        User current = securityUtils.currentUser();
        Ticket ticket = mustTicket(id);
        // 客服处理完成后，状态进入“待确认”，等待用户最终确认完成。
        ensureAssignedHandler(current, ticket);
        ensureNotTerminal(ticket);
        if (!TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            throw new RuntimeException(ErrorMessage.TICKET_COMPLETE_STATUS_INVALID);
        }
        String result = processDto == null ? null : processDto.getHandleResult();
        if (!StringUtils.hasText(result)) {
            throw new RuntimeException(ErrorMessage.TICKET_HANDLE_RESULT_EMPTY);
        }
        if (StringUtils.hasText(processDto.getContent())) {
            // 客服提交处理结果时可顺带追加一条沟通记录，便于用户查看处理说明。
            TicketComment comment = new TicketComment();
            comment.setTicketId(id);
            comment.setUserId(current.getId());
            comment.setParentId(BusinessConstant.ROOT_COMMENT_PARENT_ID);
            comment.setContent(processDto.getContent());
            ticketCommentMapper.insert(comment);
        }
        Ticket update = new Ticket();
        update.setId(id);
        update.setStatus(TicketStatus.WAIT_CONFIRM);
        update.setHandleResult(result);
        update.setFinishTime(LocalDateTime.now());
        ticketMapper.update(update);
        addLog(id, current.getId(),
                TicketOperationConstant.COMPLETE,
                TicketOperationConstant.DESC_COMPLETE, ticket.getStatus(),
                TicketStatus.WAIT_CONFIRM, ticket.getAssigneeId(), ticket.getAssigneeId());
        addMessage(ticket.getCreatorId(), current.getId(), id,
                MessageConstant.TYPE_TICKET_CONFIRM,
                MessageConstant.TITLE_TICKET_CONFIRM,
                MessageConstant.CONTENT_TICKET_CONFIRM);
    }

    /**
     * 完成工单，根据当前用户角色校验不同的完成条件。
     */
    @Override
    @Transactional
    public void close(Long id) {
        User current = securityUtils.currentUser();
        Ticket ticket = mustTicket(id);
        // 用户只能关闭自己的待确认工单；客服只能关闭自己正在处理的工单；管理员不受此流程限制。
        if (RoleConstant.USER.equals(current.getRole())) {
            if (!ticket.getCreatorId().equals(current.getId()) || !TicketStatus.WAIT_CONFIRM.equals(ticket.getStatus())) {
                throw new RuntimeException(ErrorMessage.TICKET_CLOSE_OWN_WAIT_CONFIRM_ONLY);
            }
        } else if (RoleConstant.HANDLER.equals(current.getRole())) {
            ensureAssignedHandler(current, ticket);
            if (!TicketStatus.PROCESSING.equals(ticket.getStatus())) {
                throw new RuntimeException(ErrorMessage.TICKET_HANDLER_CLOSE_PROCESSING_ONLY);
            }
        } else if (!RoleConstant.ADMIN.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.TICKET_CLOSE_ACCESS_DENIED);
        }
        ensureNotTerminal(ticket);
        Ticket update = new Ticket();
        update.setId(id);
        update.setStatus(TicketStatus.COMPLETED);
        update.setCloseTime(LocalDateTime.now());
        ticketMapper.update(update);
        addLog(id, current.getId(),
                TicketOperationConstant.CLOSE, TicketOperationConstant.DESC_CLOSE,
                ticket.getStatus(), TicketStatus.COMPLETED, ticket.getAssigneeId(), ticket.getAssigneeId());
        if (ticket.getCreatorId() != null && !ticket.getCreatorId().equals(current.getId())) {
            addMessage(ticket.getCreatorId(), current.getId(), id,
                    MessageConstant.TYPE_TICKET_CLOSE,
                    MessageConstant.TITLE_TICKET_CLOSE,
                    MessageConstant.CONTENT_TICKET_CLOSE);
        }
        if (ticket.getAssigneeId() != null && !ticket.getAssigneeId().equals(current.getId())) {
            addMessage(ticket.getAssigneeId(), current.getId(), id,
                    MessageConstant.TYPE_TICKET_CLOSE,
                    MessageConstant.TITLE_TICKET_CLOSE,
                    MessageConstant.CONTENT_TICKET_CLOSE);
        }
    }

    /**
     * 管理员驳回待受理工单，并向工单创建人发送驳回通知。
     */
    @Override
    @Transactional
    public void reject(Long id, TicketRejectDto rejectDto) {
        User current = securityUtils.currentUser();
        // 驳回只发生在待受理阶段，避免处理中或已完成工单被管理员直接打回。
        ensureAdmin(current);
        Ticket ticket = mustTicket(id);
        ensureNotTerminal(ticket);
        if (!TicketStatus.PENDING.equals(ticket.getStatus())) {
            throw new RuntimeException(ErrorMessage.TICKET_REJECT_PENDING_ONLY);
        }
        Ticket update = new Ticket();
        update.setId(id);
        update.setStatus(TicketStatus.REJECTED);
        update.setHandleResult(rejectDto == null ? null : rejectDto.getReason());
        ticketMapper.update(update);
        addLog(id, current.getId(),
                TicketOperationConstant.REJECT, TicketOperationConstant.DESC_REJECT,
                ticket.getStatus(), TicketStatus.REJECTED, ticket.getAssigneeId(), ticket.getAssigneeId());
        addMessage(ticket.getCreatorId(), current.getId(), id,
                MessageConstant.TYPE_TICKET_REJECT,
                MessageConstant.TITLE_TICKET_REJECT,
                MessageConstant.CONTENT_TICKET_REJECT);
    }

    /**
     * 用户催促待受理或处理中的工单，每个状态超过 10 分钟后只能催促一次。
     */
    @Override
    @Transactional
    public void urge(Long id) {
        User current = securityUtils.currentUser();
        if (!RoleConstant.USER.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.TICKET_URGE_USER_ONLY);
        }
        Ticket ticket = mustTicket(id);
        if (!current.getId().equals(ticket.getCreatorId())) {
            throw new RuntimeException(ErrorMessage.TICKET_URGE_OWN_ONLY);
        }
        String operationType = urgeOperationType(ticket.getStatus());
        if (operationType == null) {
            throw new RuntimeException(ErrorMessage.TICKET_URGE_STATUS_INVALID);
        }
        LocalDateTime statusStartTime = urgeStatusStartTime(ticket);
        if (statusStartTime == null || Duration.between(statusStartTime, LocalDateTime.now()).toMinutes() < 10) {
            throw new RuntimeException(ErrorMessage.TICKET_URGE_TOO_EARLY);
        }
        if (ticketFlowLogMapper.countByTicketAndType(id, operationType) > 0) {
            throw new RuntimeException(ErrorMessage.TICKET_URGE_ALREADY_USED);
        }
        String desc = TicketStatus.PENDING.equals(ticket.getStatus())
                ? TicketOperationConstant.DESC_URGE_PENDING
                : TicketOperationConstant.DESC_URGE_PROCESSING;
        addLog(id, current.getId(), operationType, desc,
                ticket.getStatus(), ticket.getStatus(), ticket.getAssigneeId(), ticket.getAssigneeId());

        if (TicketStatus.PENDING.equals(ticket.getStatus())) {
            for (User admin : userMapper.selectByRole(RoleConstant.ADMIN)) {
                addMessage(admin.getId(), current.getId(), id,
                        MessageConstant.TYPE_TICKET_URGE,
                        MessageConstant.TITLE_TICKET_URGE,
                        MessageConstant.CONTENT_TICKET_URGE_PENDING);
            }
        } else if (ticket.getAssigneeId() != null) {
            addMessage(ticket.getAssigneeId(), current.getId(), id,
                    MessageConstant.TYPE_TICKET_URGE,
                    MessageConstant.TITLE_TICKET_URGE,
                    MessageConstant.CONTENT_TICKET_URGE_PROCESSING);
        }
    }

    /**
     * 查询工单回复列表，先复用详情权限校验保证只能查看有权工单。
     */
    @Override
    public List<TicketCommentVo> comments(Long id) {
        detail(id);
        return ticketCommentMapper.selectByTicketId(id);
    }

    /**
     * 查询工单流转日志，并补充日志中的前后状态中文名称。
     */
    @Override
    public List<TicketFlowLogVo> logs(Long id) {
        detail(id);
        List<TicketFlowLogVo> logs = ticketFlowLogMapper.selectByTicketId(id);
        logs.forEach(this::fillLogStatusName);
        return logs;
    }

    /**
     * 根据 id 加载工单；不存在时统一抛出业务异常。
     */
    private Ticket mustTicket(Long id) {
        // 业务层统一通过这个方法加载工单，保证不存在时抛出的错误一致。
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new RuntimeException(ErrorMessage.TICKET_NOT_FOUND);
        }
        return ticket;
    }

    /**
     * 校验当前用户是否可以查看该工单详情或相关记录。
     */
    private void ensureReadable(TicketVo ticket) {
        User current = securityUtils.currentUser();
        // 管理员可以查看所有工单，因此这里只限制普通用户和客服。
        if (RoleConstant.USER.equals(current.getRole()) && !ticket.getCreatorId().equals(current.getId())) {
            throw new RuntimeException(ErrorMessage.TICKET_VIEW_OWN_ONLY);
        }
        if (RoleConstant.HANDLER.equals(current.getRole()) && !current.getId().equals(ticket.getAssigneeId())) {
            throw new RuntimeException(ErrorMessage.TICKET_VIEW_ASSIGNED_ONLY);
        }
    }

    /**
     * 校验当前用户是否可以在该工单下新增回复。
     */
    private void ensureCanComment(User current, Ticket ticket) {
        ensureNotTerminal(ticket);
        // 终态之外，只有与该工单有关的人可以参与沟通。
        if (RoleConstant.USER.equals(current.getRole()) && ticket.getCreatorId().equals(current.getId())) {
            return;
        }
        if (RoleConstant.HANDLER.equals(current.getRole()) && current.getId().equals(ticket.getAssigneeId())) {
            return;
        }
        if (RoleConstant.ADMIN.equals(current.getRole())) {
            return;
        }
        throw new RuntimeException(ErrorMessage.TICKET_COMMENT_ACCESS_DENIED);
    }

    /**
     * 校验当前用户是否为该工单已分配的客服处理人。
     */
    private void ensureAssignedHandler(User current, Ticket ticket) {
        if (!RoleConstant.HANDLER.equals(current.getRole()) || !current.getId().equals(ticket.getAssigneeId())) {
            throw new RuntimeException(ErrorMessage.TICKET_HANDLE_ASSIGNED_ONLY);
        }
    }

    /**
     * 校验当前用户是否具备管理员权限。
     */
    private void ensureAdmin(User current) {
        if (!RoleConstant.ADMIN.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.ADMIN_REQUIRED);
        }
    }

    /**
     * 校验工单是否仍可修改，已完成或已驳回的工单不允许继续流转。
     */
    private void ensureNotTerminal(Ticket ticket) {
        if (TicketStatus.terminal(ticket.getStatus())) {
            throw new RuntimeException(ErrorMessage.TICKET_TERMINAL_CANNOT_MODIFY);
        }
    }

    /**
     * 写入工单流转日志，记录状态和处理人变更前后的信息。
     */
    private void addLog(Long ticketId, Long operatorId, String type, String desc,
                        Integer beforeStatus, Integer afterStatus, Long beforeAssigneeId, Long afterAssigneeId) {
        // 流转日志记录状态和处理人的前后值，是排查工单流转问题的主要依据。
        TicketFlowLog log = new TicketFlowLog();
        log.setTicketId(ticketId);
        log.setOperatorId(operatorId);
        log.setOperationType(type);
        log.setOperationDesc(desc);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setBeforeAssigneeId(beforeAssigneeId);
        log.setAfterAssigneeId(afterAssigneeId);
        ticketFlowLogMapper.insert(log);
    }

    /**
     * 创建站内消息对象并入库，避免调用处直接传入一长串字段。
     */
    private void addMessage(Long receiverId, Long senderId, Long ticketId, String messageType, String title, String content) {
        // 统一通过 MessageService 创建消息，入库后会自动发布 Redis/SSE 实时提醒。
        messageService.create(receiverId, senderId, ticketId, messageType, title, content);
    }

    /**
     * 生成当天递增的工单编号，格式为 FT + 日期 + 四位序号。
     */
    private String nextTicketNo() {
        String prefix = BusinessConstant.TICKET_NO_PREFIX + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String last = ticketMapper.selectLastTicketNo(prefix);
        int next = 1;
        // 当天已有工单时取最大编号加一；当天没有记录时从 0001 开始。
        if (StringUtils.hasText(last) && last.length() >= prefix.length() + BusinessConstant.TICKET_NO_SERIAL_LENGTH) {
            next = Integer.parseInt(last.substring(prefix.length())) + 1;
        }
        return prefix + String.format(BusinessConstant.TICKET_NO_SERIAL_FORMAT, next);
    }

    /**
     * 为工单视图对象填充状态对应的中文名称。
     */
    private void fillStatusName(TicketVo ticket) {
        // VO 中补充中文状态名，前端无需再维护一份状态字典。
        ticket.setStatusName(TicketStatus.nameOf(ticket.getStatus()));
    }

    private void fillUrgeInfo(TicketVo ticket) {
        User current = securityUtils.currentUser();
        ticket.setCanUrge(false);
        if (!RoleConstant.USER.equals(current.getRole()) || !current.getId().equals(ticket.getCreatorId())) {
            return;
        }
        String operationType = urgeOperationType(ticket.getStatus());
        if (operationType == null) {
            ticket.setUrgeMessage(ErrorMessage.TICKET_URGE_STATUS_INVALID);
            return;
        }
        LocalDateTime statusStartTime = urgeStatusStartTime(ticket);
        long minutes = statusStartTime == null ? 0 : Duration.between(statusStartTime, LocalDateTime.now()).toMinutes();
        if (minutes < 10) {
            ticket.setUrgeMessage(ErrorMessage.TICKET_URGE_WAIT_10_MINUTES);
            return;
        }
        if (ticketFlowLogMapper.countByTicketAndType(ticket.getId(), operationType) > 0) {
            ticket.setUrgeMessage(ErrorMessage.TICKET_URGE_ALREADY_USED);
            return;
        }
        ticket.setCanUrge(true);
        ticket.setUrgeMessage(ErrorMessage.TICKET_URGE_AVAILABLE);
    }

    private String urgeOperationType(Integer status) {
        if (TicketStatus.PENDING.equals(status)) {
            return TicketOperationConstant.URGE_PENDING;
        }
        if (TicketStatus.PROCESSING.equals(status)) {
            return TicketOperationConstant.URGE_PROCESSING;
        }
        return null;
    }

    private LocalDateTime urgeStatusStartTime(Ticket ticket) {
        if (TicketStatus.PENDING.equals(ticket.getStatus())) {
            return ticket.getCreateTime();
        }
        if (TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            LocalDateTime startTime = ticketFlowLogMapper.selectLatestStatusStartTime(ticket.getId(), TicketStatus.PROCESSING);
            return startTime == null ? ticket.getUpdateTime() : startTime;
        }
        return null;
    }

    private LocalDateTime urgeStatusStartTime(TicketVo ticket) {
        if (TicketStatus.PENDING.equals(ticket.getStatus())) {
            return ticket.getCreateTime();
        }
        if (TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            LocalDateTime startTime = ticketFlowLogMapper.selectLatestStatusStartTime(ticket.getId(), TicketStatus.PROCESSING);
            return startTime == null ? ticket.getUpdateTime() : startTime;
        }
        return null;
    }

    /**
     * 为流转日志视图对象填充变更前后状态的中文名称。
     */
    private void fillLogStatusName(TicketFlowLogVo log) {
        log.setBeforeStatusName(TicketStatus.nameOf(log.getBeforeStatus()));
        log.setAfterStatusName(TicketStatus.nameOf(log.getAfterStatus()));
    }
}
