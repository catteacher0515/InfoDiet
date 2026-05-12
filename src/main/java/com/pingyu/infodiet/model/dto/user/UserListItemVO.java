package com.pingyu.infodiet.model.dto.user;

import lombok.Builder;
import lombok.Data;

/**
 * 用户列表项
 */
@Data
@Builder
public class UserListItemVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 账号
     */
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private Integer status;
}
