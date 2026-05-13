package com.onik.flowticket.service;

import com.onik.flowticket.dto.TicketCategoryDto;
import com.onik.flowticket.entity.TicketCategory;

import java.util.List;

public interface TicketCategoryService {
    List<TicketCategory> list(Boolean onlyEnabled);

    void create(TicketCategoryDto dto);

    void update(TicketCategoryDto dto);

    void delete(Long id);
}
