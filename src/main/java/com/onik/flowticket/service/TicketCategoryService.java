package com.onik.flowticket.service;

import com.onik.flowticket.dto.TicketCategoryDto;
import com.onik.flowticket.dto.TicketCategoryQueryDto;
import com.onik.flowticket.entity.TicketCategory;

import java.util.List;

public interface TicketCategoryService {
    List<TicketCategory> list(TicketCategoryQueryDto queryDto);

    void create(TicketCategoryDto dto);

    void update(TicketCategoryDto dto);

    void delete(Long id);
}
