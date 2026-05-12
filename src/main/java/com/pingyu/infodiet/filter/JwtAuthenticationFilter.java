package com.pingyu.infodiet.filter;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorization = request.getHeader("Authorization");
            if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
                String token = authorization.substring("Bearer ".length());
                if (jwtUtils.verifyToken(token)) {
                    LoginUser loginUser = jwtUtils.parseToken(token);
                    LoginUserContext.set(loginUser);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            LoginUserContext.clear();
        }
    }
}
