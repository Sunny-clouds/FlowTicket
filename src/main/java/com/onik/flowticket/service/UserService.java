package com.onik.flowticket.service;

import com.onik.flowticket.common.PageResult;
import com.onik.flowticket.dto.UserDto;
import com.onik.flowticket.dto.UserLoginDto;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.vo.UserLoginVo;

/**
 * 用户业务接口。
 */
public interface UserService {

    /**
     * 登录。
     * @param userLoginDto
     * @return
     */
    UserLoginVo getByUserName(UserLoginDto userLoginDto);

    /**
     * 分页查询用户列表。
     */
    PageResult<User> page(Integer pageNum, Integer pageSize);

    /**
     * 根据用户 id 查询单个用户。
     */
    User getById(Long id);

    /**
     * 新增用户。
     */
    void save(UserDto userDto);

    /**
     * 普通用户注册。
     */
    void register(UserDto userDto);

    /**
     * 根据id 修改用户。
     */
    void updateById(UserDto userDto);

    /**
     * 根据 id 删除用户。
     */
    void removeById(Long id);

}
