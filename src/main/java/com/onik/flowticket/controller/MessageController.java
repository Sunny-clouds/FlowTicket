package com.onik.flowticket.controller;

import com.onik.flowticket.common.Result;
import com.onik.flowticket.entity.Message;
import com.onik.flowticket.service.MessageRealtimeService;
import com.onik.flowticket.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "消息提醒接口", description = "站内消息查询、已读状态和实时提醒接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageRealtimeService messageRealtimeService;

    @Operation(summary = "查询我的消息列表")
    @GetMapping
    public Result<List<Message>> list() {
        return Result.success(messageService.listCurrentUserMessages());
    }

    @Operation(summary = "查询我的未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.countCurrentUserUnread());
    }

    @Operation(summary = "标记单条消息为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead();
        return Result.success();
    }

    @Operation(summary = "订阅实时消息提醒")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return messageRealtimeService.connect();
    }
}
