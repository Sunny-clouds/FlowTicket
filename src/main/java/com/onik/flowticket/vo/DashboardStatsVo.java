package com.onik.flowticket.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsVo {
    private Long todayNewTickets;
    private Long pendingTickets;
    private Long processingTickets;
    private Long waitConfirmTickets;
    private Long completedTickets;
    private Long rejectedTickets;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> sevenDayTrend;
    private List<Map<String, Object>> handlerRanking;
}
