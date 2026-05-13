package com.onik.flowticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 支持 localhost 任意端口，例如 5173、3000、8081。
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // 允许常用 REST 方法，OPTIONS 用于跨域预检。
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 允许所有请求头，包含 Authorization。
        config.setAllowedHeaders(List.of("*"));

        // 如果后端返回 Authorization 响应头，前端可以读取。
        config.setExposedHeaders(List.of("Authorization"));

        // 使用 Bearer token 时通常不需要 Cookie；这里关闭凭证，配置更简单。
        config.setAllowCredentials(false);

        // 预检请求缓存 1 小时。
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
