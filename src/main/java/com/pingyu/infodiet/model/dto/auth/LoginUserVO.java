package com.pingyu.infodiet.model.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 登录用户视图
 */
@Data
@Builder
public class LoginUserVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * token
     */
    private String token;
}
