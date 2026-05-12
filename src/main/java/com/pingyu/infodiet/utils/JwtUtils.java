package com.pingyu.infodiet.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.pingyu.infodiet.config.JwtProperties;
import com.pingyu.infodiet.model.auth.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 */
@Component
public class JwtUtils {

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 生成 token
     */
    public String createToken(Long userId, String username, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("exp", DateUtil.offsetSecond(new Date(), (int) jwtProperties.getExpireSeconds()));
        return JWTUtil.createToken(payload, secretBytes());
    }

    /**
     * 校验 token
     */
    public boolean verifyToken(String token) {
        return StrUtil.isNotBlank(token) && JWTUtil.verify(token, secretBytes());
    }

    /**
     * 解析 token
     */
    public LoginUser parseToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object userId = jwt.getPayload("userId");
        Object username = jwt.getPayload("username");
        Object role = jwt.getPayload("role");
        return LoginUser.builder()
                .userId(userId == null ? null : Long.valueOf(String.valueOf(userId)))
                .username(username == null ? null : String.valueOf(username))
                .role(role == null ? null : String.valueOf(role))
                .build();
    }

    private byte[] secretBytes() {
        return jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    }
}
