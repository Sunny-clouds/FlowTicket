package com.onik.flowticket.mapper;

import com.onik.flowticket.entity.TicketComment;
import com.onik.flowticket.vo.TicketCommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketCommentMapper {
    List<TicketCommentVo> selectByTicketId(@Param("ticketId") Long ticketId);

    void insert(TicketComment comment);
}
