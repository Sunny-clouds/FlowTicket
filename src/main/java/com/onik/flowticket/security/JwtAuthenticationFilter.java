package com.onik.flowticket.security;

import com.onik.flowticket.common.RedisKeyConstant;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 * 负责从 Authorization 请求头中读取 Bearer token，解析登录用户和角色后写入 SecurityContext。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_QUERY = "token";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            Object userId = claims.get("userId");
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            validateRedisToken(userId, token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(role)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"登录状态已失效，请重新登录\",\"data\":null}");
        }
    }

    /**
     * 校验 Redis 中保存的 token，用户重新登录后旧 token 会被新 token 覆盖并失效。
     */
    private void validateRedisToken(Object userId, String token) {
        if (userId == null) {
            throw new RuntimeException("token 缺少用户标识");
        }
        String redisToken = redisTemplate.opsForValue().get(RedisKeyConstant.USER_TOKEN_PREFIX + userId);
        if (!token.equals(redisToken)) {
            throw new RuntimeException("token 已失效");
        }
    }

    /**
     * 读取 Authorization: Bearer xxx 中的 token。
     */
    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        // EventSource 不能自定义 Authorization 请求头，实时消息订阅允许通过 query 参数传 token。
        String queryToken = request.getParameter(TOKEN_QUERY);
        return StringUtils.hasText(queryToken) ? queryToken : null;
    }
}
