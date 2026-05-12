package com.pingyu.infodiet.model.dto.auth;

import lombok.Data;

/**
 * 注册请求
 */
@Data
public class AuthRegisterRequest {

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;
}
