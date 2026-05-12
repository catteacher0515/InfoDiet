package com.pingyu.infodiet.config;

import com.pingyu.infodiet.filter.JwtAuthenticationFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 认证配置
 */
@Configuration
public class WebMvcAuthConfig implements WebMvcConfigurer {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // no-op
    }
}
