package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.auth.AdminCreateUserRequest;
import com.pingyu.infodiet.model.dto.auth.AuthLoginRequest;
import com.pingyu.infodiet.model.dto.auth.AuthRegisterRequest;
import com.pingyu.infodiet.model.dto.auth.LoginUserVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 注册
     */
    Long register(AuthRegisterRequest request);

    /**
     * 登录
     */
    LoginUserVO login(AuthLoginRequest request);

    /**
     * 查询当前用户
     */
    LoginUserVO getCurrentUser();

    /**
     * 退出登录
     */
    boolean logout();

    /**
     * 管理员创建用户
     */
    Long adminCreateUser(AdminCreateUserRequest request);
}
