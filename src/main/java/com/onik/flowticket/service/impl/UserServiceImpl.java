package com.onik.flowticket.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.onik.flowticket.common.*;
import com.onik.flowticket.dto.UserDto;
import com.onik.flowticket.dto.UserLoginDto;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.mapper.UserMapper;
import com.onik.flowticket.security.JwtUtil;
import com.onik.flowticket.service.UserService;
import com.onik.flowticket.vo.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表，供后台用户管理页面按页展示账号数据。
     */
    @Override
    public PageResult<User> page(Integer pageNum, Integer pageSize) {
        // PageHelper 会拦截紧随其后的 MyBatis 查询，并自动追加分页 SQL。
        PageHelper.startPage(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        List<User> records = userMapper.selectList();
        PageInfo<User> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据用户 id 查询单个用户详情，用于编辑或查看用户基础信息。
     */
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 新增用户账号，校验用户名唯一性并对密码加密后入库。
     */
    @Override
    public void save(UserDto userDto) {
        // 管理员新增用户和注册共用这套落库逻辑，因此这里统一做必填和用户名唯一性校验。
        if (userDto == null || !StringUtils.hasText(userDto.getUsername()) || !StringUtils.hasText(userDto.getPassword())) {
            throw new RuntimeException(ErrorMessage.PARAM_EMPTY);
        }
        if (userMapper.selectByUserName(userDto.getUsername()) != null) {
            throw new RuntimeException(ErrorMessage.USERNAME_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        // 数据库只保存 BCrypt 密文，登录时通过 passwordEncoder.matches 进行比对。
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if (user.getRole() == null) {
            user.setRole(RoleConstant.USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(StatusConstant.STATUS);
        }
        user.setDeleted(DeletedConstant.NOT_DELETED);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 注册普通用户账号，默认分配普通用户角色和启用状态。
     */
    @Override
    public void register(UserDto userDto) {
        // 对外注册入口强制创建普通用户，避免用户通过请求体伪造管理员或客服角色。
        if (userDto == null) {
            throw new RuntimeException(ErrorMessage.PARAM_EMPTY);
        }
        userDto.setRole(RoleConstant.USER);
        userDto.setStatus(StatusConstant.STATUS);
        save(userDto);
    }

    /**
     * 更新用户资料；当传入新密码时同步重新加密保存。
     */
    @Override
    public void updateById(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) {
            throw new RuntimeException(ErrorMessage.PARAM_EMPTY);
        }
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        // 修改用户时密码为空表示不修改密码，避免把原密码覆盖成空值。
        if (StringUtils.hasText(userDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }

    /**
     * 根据用户 id 逻辑删除账号，保留数据库历史记录。
     */
    @Override
    public void removeById(Long id) {
        userMapper.logicDeleteById(id);
    }

    /**
     * 处理用户登录，完成账号密码校验、登录时间更新并生成 JWT。
     */
    @Override
    public UserLoginVo getByUserName(UserLoginDto userLoginDto) {
        if (userLoginDto == null || !StringUtils.hasText(userLoginDto.getUsername()) || !StringUtils.hasText(userLoginDto.getPassword())) {
            throw new RuntimeException(ErrorMessage.PARAM_EMPTY);
        }
        User user = userMapper.selectByUserName(userLoginDto.getUsername());
        if (user != null && user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException(ErrorMessage.USER_DISABLED);
        }
        if (user != null && passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            // 登录成功后刷新最后登录时间，并把用户 id、用户名、角色写入 JWT claims
            userMapper.updateLastLoginById(user.getId());
            UserLoginVo userLoginVo = new UserLoginVo();
            BeanUtils.copyProperties(user, userLoginVo);
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", "ROLE_" + user.getRole());
            userLoginVo.setToken(JwtUtil.generateToken(claims));
            return userLoginVo;
        }
        throw new RuntimeException(ErrorMessage.USERNAME_OR_PASSWORD_ERROR);
    }
}
