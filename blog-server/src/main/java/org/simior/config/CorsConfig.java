package org.simior.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${blog.cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 根据配置决定是否允许特定域名
        // 生产环境必须显式配置允许的来源，未配置时拒绝所有跨域请求
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            config.addAllowedOriginPattern("http://localhost:*");
        } else {
            for (String origin : allowedOrigins.split(",")) {
                config.addAllowedOriginPattern(origin.trim());
            }
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
