package com.onik.flowticket.mapper;

import com.onik.flowticket.dto.TicketCategoryQueryDto;
import com.onik.flowticket.entity.TicketCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketCategoryMapper {
    List<TicketCategory> selectList(TicketCategoryQueryDto queryDto);

    TicketCategory selectById(@Param("id") Long id);

    void insert(TicketCategory category);

    void updateById(TicketCategory category);

    void logicDeleteById(@Param("id") Long id);
}
