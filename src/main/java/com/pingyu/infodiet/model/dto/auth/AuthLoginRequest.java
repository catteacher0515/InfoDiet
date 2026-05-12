package com.pingyu.infodiet.model.dto.auth;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class AuthLoginRequest {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;
}
