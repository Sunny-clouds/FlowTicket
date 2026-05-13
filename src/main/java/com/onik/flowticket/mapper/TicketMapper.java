package com.onik.flowticket.mapper;

import com.onik.flowticket.dto.TicketQueryDto;
import com.onik.flowticket.entity.Ticket;
import com.onik.flowticket.vo.TicketVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TicketMapper {
    List<TicketVo> selectPage(TicketQueryDto queryDto);

    TicketVo selectVoById(@Param("id") Long id);

    Ticket selectById(@Param("id") Long id);

    void insert(Ticket ticket);

    void update(Ticket ticket);

    String selectLastTicketNo(@Param("prefix") String prefix);

    Long countTodayNew();

    Long countByStatus(@Param("status") Integer status);

    List<Map<String, Object>> categoryDistribution();

    List<Map<String, Object>> sevenDayTrend();

    List<Map<String, Object>> handlerRanking();
}
