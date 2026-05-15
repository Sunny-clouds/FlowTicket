package com.onik.flowticket.mapper;

import com.onik.flowticket.entity.TicketFlowLog;
import com.onik.flowticket.vo.TicketFlowLogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TicketFlowLogMapper {
    void insert(TicketFlowLog log);

    List<TicketFlowLogVo> selectByTicketId(@Param("ticketId") Long ticketId);

    List<TicketFlowLogVo> selectAll();

    List<TicketFlowLogVo> selectByOperatorId(@Param("operatorId") Long operatorId);

    Long countByTicketAndType(@Param("ticketId") Long ticketId, @Param("operationType") String operationType);

    LocalDateTime selectLatestStatusStartTime(@Param("ticketId") Long ticketId, @Param("status") Integer status);
}
