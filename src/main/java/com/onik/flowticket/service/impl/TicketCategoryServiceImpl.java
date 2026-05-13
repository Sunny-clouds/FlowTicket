package com.onik.flowticket.service.impl;

import com.onik.flowticket.common.*;
import com.onik.flowticket.dto.TicketCategoryDto;
import com.onik.flowticket.entity.TicketCategory;
import com.onik.flowticket.mapper.TicketCategoryMapper;
import com.onik.flowticket.service.TicketCategoryService;
import com.onik.flowticket.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TicketCategoryServiceImpl implements TicketCategoryService {
    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;
    @Autowired
    private SecurityUtils securityUtils;


    /**
     * 查询工单分类列表，可按需要只返回已启用的分类。
     */
    @Override
    public List<TicketCategory> list(Boolean onlyEnabled) {
        // onlyEnabled=true 时只返回启用分类，适合前端提交工单时使用。
        return ticketCategoryMapper.selectList(onlyEnabled);
    }

    /**
     * 创建新的工单分类，只有管理员可以执行该操作。
     */
    @Override
    public void create(TicketCategoryDto dto) {
        // 分类属于后台配置，只允许管理员维护。
        ensureAdmin();
        if (dto == null || !StringUtils.hasText(dto.getCategoryName())) {
            throw new RuntimeException(ErrorMessage.CATEGORY_NAME_EMPTY);
        }
        TicketCategory category = new TicketCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getSort() == null) {
            category.setSort(BusinessConstant.DEFAULT_CATEGORY_SORT);
        }
        // 未显式传状态时默认启用，避免新增分类后前端不可选。
        if (category.getStatus() == null) {
            category.setStatus(StatusConstant.STATUS);
        }
        ticketCategoryMapper.insert(category);
    }

    /**
     * 更新工单分类名称、排序和启用状态等基础配置。
     */
    @Override
    public void update(TicketCategoryDto dto) {
        // 更新使用 MyBatis 的动态 SQL，只修改请求体中非空字段。
        ensureAdmin();
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException(ErrorMessage.CATEGORY_ID_EMPTY);
        }
        TicketCategory category = new TicketCategory();
        BeanUtils.copyProperties(dto, category);
        ticketCategoryMapper.updateById(category);
    }

    /**
     * 逻辑删除指定工单分类，使其不再参与后续工单创建。
     */
    @Override
    public void delete(Long id) {
        ensureAdmin();
        ticketCategoryMapper.logicDeleteById(id);
    }

    /**
     * 校验当前登录用户是否为管理员，防止普通用户维护分类配置。
     */
    private void ensureAdmin() {
        // 分类管理接口统一走这里做角色校验，避免每个方法重复写判断。
        if (!RoleConstant.ADMIN.equals(securityUtils.currentUser().getRole())) {
            throw new RuntimeException(ErrorMessage.ADMIN_REQUIRED);
        }
    }
}
