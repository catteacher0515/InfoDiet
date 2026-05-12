package com.pingyu.infodiet.model.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户
 */
@Data
@Builder
public class LoginUser {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 角色
     */
    private String role;
}
