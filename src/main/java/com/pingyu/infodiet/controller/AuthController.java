package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.auth.AdminCreateUserRequest;
import com.pingyu.infodiet.model.dto.auth.AuthLoginRequest;
import com.pingyu.infodiet.model.dto.auth.AuthRegisterRequest;
import com.pingyu.infodiet.model.dto.auth.LoginUserVO;
import com.pingyu.infodiet.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody AuthRegisterRequest request) {
        return ResultUtils.success(authService.register(request));
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody AuthLoginRequest request) {
        return ResultUtils.success(authService.login(request));
    }

    /**
     * 当前用户
     */
    @GetMapping("/me")
    public BaseResponse<LoginUserVO> getCurrentUser() {
        return ResultUtils.success(authService.getCurrentUser());
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        return ResultUtils.success(authService.logout());
    }

    /**
     * 管理员创建用户
     */
    @PostMapping("/admin/create")
    public BaseResponse<Long> adminCreateUser(@RequestBody AdminCreateUserRequest request) {
        return ResultUtils.success(authService.adminCreateUser(request));
    }
}
