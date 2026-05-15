package com.onik.flowticket.mapper;

import com.onik.flowticket.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper。
 */
@Mapper
public interface UserMapper {
    /**
     * 查询所有用户。
     */
    List<User> selectList();

    /**
     * 根据 id 查询用户。
     */
    User selectById(@Param("id") Long id);

    /**
     * 新增用户。
     */
    void insert(User user);

    /**
     * 根据 id 修改用户。
     */
    void updateById(User user);

    /**
     * 根据 id 逻辑删除用户。
     */
    void logicDeleteById(@Param("id") Long id);

    /**
     * 根据用户名查询用户。
     * @param username
     * @return
     */
    User selectByUserName(@Param("username") String username);

    /**
     * 根据用户id更新最后登陆时间
     * @param id
     */
    void updateLastLoginById(Long id);

    /**
     * 根据角色查询用户，用于给管理员或客服批量发送消息。
     */
    List<User> selectByRole(@Param("role") String role);
}
