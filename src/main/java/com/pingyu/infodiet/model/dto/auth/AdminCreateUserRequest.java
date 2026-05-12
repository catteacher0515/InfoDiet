package com.pingyu.infodiet.model.dto.auth;

import lombok.Data;

/**
 * 管理员创建用户请求
 */
@Data
public class AdminCreateUserRequest {

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

    /**
     * 角色
     */
    private String role;
}
