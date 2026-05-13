package com.onik.flowticket.utils;

import com.onik.flowticket.common.ErrorMessage;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    @Autowired
    private UserMapper userMapper;

    public User currentUser() {
        // JWT 过滤器会把用户名放进 SecurityContext，这里再查一次数据库拿到完整用户信息和最新状态。
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException(ErrorMessage.UNAUTHORIZED);
        }
        User user = userMapper.selectByUserName(authentication.getName());
        if (user == null) {
            throw new RuntimeException(ErrorMessage.LOGIN_USER_NOT_FOUND);
        }
        // 账号被禁用后，即使 token 还没过期，也不能继续访问业务接口。
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException(ErrorMessage.USER_DISABLED);
        }
        return user;
    }
}
