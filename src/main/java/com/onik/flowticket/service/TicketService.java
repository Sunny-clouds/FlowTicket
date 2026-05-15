package com.onik.flowticket.service;

import com.onik.flowticket.common.PageResult;
import com.onik.flowticket.dto.*;
import com.onik.flowticket.vo.TicketCommentVo;
import com.onik.flowticket.vo.TicketFlowLogVo;
import com.onik.flowticket.vo.TicketVo;

import java.util.List;

public interface TicketService {
    PageResult<TicketVo> page(TicketQueryDto queryDto);

    TicketVo detail(Long id);

    void create(TicketCreateDto createDto);

    void assign(Long id, TicketAssignDto assignDto);

    void updatePriority(Long id, PriorityUpdateDto priorityUpdateDto);

    void addComment(Long id, TicketCommentDto commentDto);

    void complete(Long id, TicketProcessDto processDto);

    void close(Long id);

    void reject(Long id, TicketRejectDto rejectDto);

    void urge(Long id);

    List<TicketCommentVo> comments(Long id);

    List<TicketFlowLogVo> logs(Long id);
}
