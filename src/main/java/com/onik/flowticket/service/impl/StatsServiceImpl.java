package com.onik.flowticket.service.impl;

import com.onik.flowticket.common.ErrorMessage;
import com.onik.flowticket.common.RoleConstant;
import com.onik.flowticket.common.TicketStatus;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.mapper.TicketFlowLogMapper;
import com.onik.flowticket.mapper.TicketMapper;
import com.onik.flowticket.service.StatsService;
import com.onik.flowticket.utils.SecurityUtils;
import com.onik.flowticket.vo.DashboardStatsVo;
import com.onik.flowticket.vo.TicketFlowLogVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {
    @Autowired
    private TicketMapper ticketMapper;
    @Autowired
    private TicketFlowLogMapper ticketFlowLogMapper;
    @Autowired
    private SecurityUtils securityUtils;

    /**
     * 汇总后台首页需要展示的工单数量和待处理统计数据。
     */
    @Override
    public DashboardStatsVo dashboard() {
        // 统计面板只提供给管理员，数据来源都在 TicketMapper 中聚合查询。
        ensureAdmin();
        DashboardStatsVo stats = new DashboardStatsVo();
        stats.setTodayNewTickets(ticketMapper.countTodayNew());
        stats.setPendingTickets(ticketMapper.countByStatus(TicketStatus.PENDING));
        stats.setProcessingTickets(ticketMapper.countByStatus(TicketStatus.PROCESSING));
        stats.setWaitConfirmTickets(ticketMapper.countByStatus(TicketStatus.WAIT_CONFIRM));
        stats.setCompletedTickets(ticketMapper.countByStatus(TicketStatus.COMPLETED));
        stats.setRejectedTickets(ticketMapper.countByStatus(TicketStatus.REJECTED));
        stats.setCategoryDistribution(ticketMapper.categoryDistribution());
        stats.setSevenDayTrend(ticketMapper.sevenDayTrend());
        stats.setHandlerRanking(ticketMapper.handlerRanking());
        return stats;
    }

    /**
     * 查询全部工单流转日志，供管理员审计整体操作记录。
     */
    @Override
    public List<TicketFlowLogVo> operationLogs() {
        // 管理员查看全量流转日志，用于审计和追踪工单处理过程。
        ensureAdmin();
        return ticketFlowLogMapper.selectAll();
    }

    /**
     * 查询当前客服参与处理过的工单日志，形成个人处理记录。
     */
    @Override
    public List<TicketFlowLogVo> myHandleLogs() {
        User current = securityUtils.currentUser();
        // 客服只能查看自己的处理动作，不暴露其他客服的操作记录。
        if (!RoleConstant.HANDLER.equals(current.getRole())) {
            throw new RuntimeException(ErrorMessage.HANDLER_REQUIRED);
        }
        return ticketFlowLogMapper.selectByOperatorId(current.getId());
    }

    /**
     * 校验当前用户是否具备管理员权限，用于保护统计和审计接口。
     */
    private void ensureAdmin() {
        // 后台统计和日志属于管理功能，统一限制管理员访问。
        if (!RoleConstant.ADMIN.equals(securityUtils.currentUser().getRole())) {
            throw new RuntimeException(ErrorMessage.ADMIN_REQUIRED);
        }
    }
}
