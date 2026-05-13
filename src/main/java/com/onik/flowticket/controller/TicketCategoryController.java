package com.onik.flowticket.controller;

import com.onik.flowticket.common.Result;
import com.onik.flowticket.dto.TicketCategoryDto;
import com.onik.flowticket.entity.TicketCategory;
import com.onik.flowticket.service.TicketCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工单分类接口", description = "工单分类查询和管理接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/categories")
public class TicketCategoryController {

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Operation(summary = "查询分类列表")
    @GetMapping
    public Result<List<TicketCategory>> list(@RequestParam(required = false, defaultValue = "false") Boolean onlyEnabled) {
        return Result.success(ticketCategoryService.list(onlyEnabled));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Void> create(@RequestBody TicketCategoryDto dto) {
        ticketCategoryService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TicketCategoryDto dto) {
        dto.setId(id);
        ticketCategoryService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ticketCategoryService.delete(id);
        return Result.success();
    }
}
