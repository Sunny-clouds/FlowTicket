package com.onik.flowticket.controller;

import com.onik.flowticket.common.Result;
import com.onik.flowticket.service.StatsService;
import com.onik.flowticket.vo.DashboardStatsVo;
import com.onik.flowticket.vo.TicketFlowLogVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "统计接口", description = "后台统计、操作日志和客服处理记录接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Operation(summary = "管理员统计面板")
    @GetMapping("/dashboard")
    public Result<DashboardStatsVo> dashboard() {
        return Result.success(statsService.dashboard());
    }

    @Operation(summary = "查看全部操作日志")
    @GetMapping("/logs")
    public Result<List<TicketFlowLogVo>> operationLogs() {
        return Result.success(statsService.operationLogs());
    }

    @Operation(summary = "客服查看个人处理记录")
    @GetMapping("/my-handle-logs")
    public Result<List<TicketFlowLogVo>> myHandleLogs() {
        return Result.success(statsService.myHandleLogs());
    }
}
