package com.onik.flowticket.service;

import com.onik.flowticket.vo.DashboardStatsVo;
import com.onik.flowticket.vo.TicketFlowLogVo;

import java.util.List;

public interface StatsService {
    DashboardStatsVo dashboard();

    List<TicketFlowLogVo> operationLogs();

    List<TicketFlowLogVo> myHandleLogs();
}
