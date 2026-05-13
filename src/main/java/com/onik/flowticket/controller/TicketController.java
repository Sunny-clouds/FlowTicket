package com.onik.flowticket.controller;

import com.onik.flowticket.common.PageResult;
import com.onik.flowticket.common.Result;
import com.onik.flowticket.dto.*;
import com.onik.flowticket.service.TicketService;
import com.onik.flowticket.vo.TicketCommentVo;
import com.onik.flowticket.vo.TicketFlowLogVo;
import com.onik.flowticket.vo.TicketVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工单接口", description = "工单提交、查询、分配、处理、关闭和回复接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Operation(summary = "分页查询工单")
    @GetMapping
    public Result<PageResult<TicketVo>> page(TicketQueryDto queryDto) {
        return Result.success(ticketService.page(queryDto));
    }

    @Operation(summary = "查询工单详情")
    @GetMapping("/{id}")
    public Result<TicketVo> detail(@PathVariable Long id) {
        return Result.success(ticketService.detail(id));
    }

    @Operation(summary = "提交工单")
    @PostMapping
    public Result<Void> create(@RequestBody TicketCreateDto createDto) {
        ticketService.create(createDto);
        return Result.success();
    }

    @Operation(summary = "分配工单")
    @PostMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestBody TicketAssignDto assignDto) {
        ticketService.assign(id, assignDto);
        return Result.success();
    }

    @Operation(summary = "修改工单优先级")
    @PutMapping("/{id}/priority")
    public Result<Void> updatePriority(@PathVariable Long id, @RequestBody PriorityUpdateDto priorityUpdateDto) {
        ticketService.updatePriority(id, priorityUpdateDto);
        return Result.success();
    }

    @Operation(summary = "回复工单")
    @PostMapping("/{id}/comments")
    public Result<Void> addComment(@PathVariable Long id, @RequestBody TicketCommentDto commentDto) {
        ticketService.addComment(id, commentDto);
        return Result.success();
    }

    @Operation(summary = "查询工单回复记录")
    @GetMapping("/{id}/comments")
    public Result<List<TicketCommentVo>> comments(@PathVariable Long id) {
        return Result.success(ticketService.comments(id));
    }

    @Operation(summary = "客服处理完成并提交用户确认")
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody TicketProcessDto processDto) {
        ticketService.complete(id, processDto);
        return Result.success();
    }

    @Operation(summary = "关闭工单")
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        ticketService.close(id);
        return Result.success();
    }

    @Operation(summary = "驳回工单")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody TicketRejectDto rejectDto) {
        ticketService.reject(id, rejectDto);
        return Result.success();
    }

    @Operation(summary = "查询工单流转日志")
    @GetMapping("/{id}/logs")
    public Result<List<TicketFlowLogVo>> logs(@PathVariable Long id) {
        return Result.success(ticketService.logs(id));
    }
}
