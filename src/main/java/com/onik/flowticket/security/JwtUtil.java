package com.onik.flowticket.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类。
 *
 * 为了让安全代码保持简单，这里使用静态工具方法：
 * 登录成功时生成 token，请求进来时解析 token。
 */
public final class JwtUtil {
    /**
     * JWT 签名密钥，长度必须满足 HS256 要求。
     * 生产环境建议放到环境变量或配置中心。
     */
    private static final String SIGN_KEY = "FlowTicketJwtSecretKeyForHs256Authentication2026";

    /**
     * token 有效期：24 小时。
     */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SIGN_KEY.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    /**
     * 生成 JWT。
     *
     * claims 中可以放 userId、username、role 等前端或过滤器需要的信息。
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 JWT。
     *
     * token 过期、签名错误、格式错误时会抛出异常，由过滤器统一返回 401。
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
