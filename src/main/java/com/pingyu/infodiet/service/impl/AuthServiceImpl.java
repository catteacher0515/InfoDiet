package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.auth.AdminCreateUserRequest;
import com.pingyu.infodiet.model.dto.auth.AuthLoginRequest;
import com.pingyu.infodiet.model.dto.auth.AuthRegisterRequest;
import com.pingyu.infodiet.model.dto.auth.LoginUserVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.AuthService;
import com.pingyu.infodiet.service.UserProfileService;
import com.pingyu.infodiet.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    protected UserProfileService userProfileService;

    @Resource
    protected JwtUtils jwtUtils;

    /**
     * 注册
     */
    @Override
    public Long register(AuthRegisterRequest request) {
        validateRegisterRequest(request);
        if (getUserByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        UserProfile userProfile = UserProfile.builder()
                .nickname(StrUtil.trim(request.getNickname()))
                .username(normalizeUsername(request.getUsername()))
                .password(BCrypt.hashpw(request.getPassword()))
                .role(resolveRegisterRole())
                .pushChannel("feishu")
                .dailyPushLimit(10)
                .pushCooldownHours(0)
                .status(1)
                .build();
        return userProfileService.createUser(userProfile);
    }

    /**
     * 登录
     */
    @Override
    public LoginUserVO login(AuthLoginRequest request) {
        if (request == null || StrUtil.hasBlank(request.getUsername(), request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        UserProfile userProfile = getUserByUsername(request.getUsername());
        if (userProfile == null || !BCrypt.checkpw(request.getPassword(), userProfile.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        if (userProfile.getStatus() == null || userProfile.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户已禁用");
        }
        String token = jwtUtils.createToken(userProfile.getId(), userProfile.getUsername(), userProfile.getRole());
        return toLoginUserVO(userProfile, token);
    }

    /**
     * 查询当前用户
     */
    @Override
    public LoginUserVO getCurrentUser() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserProfile userProfile = userProfileService.getUserById(userId);
        if (userProfile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return toLoginUserVO(userProfile, null);
    }

    /**
     * 退出登录
     */
    @Override
    public boolean logout() {
        return true;
    }

    /**
     * 管理员创建用户
     */
    @Override
    public Long adminCreateUser(AdminCreateUserRequest request) {
        if (!"admin".equals(LoginUserContext.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (request == null || StrUtil.hasBlank(request.getNickname(), request.getUsername(), request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (getUserByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        String role = StrUtil.blankToDefault(StrUtil.trim(request.getRole()), "user").toLowerCase();
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色不合法");
        }
        UserProfile userProfile = UserProfile.builder()
                .nickname(StrUtil.trim(request.getNickname()))
                .username(normalizeUsername(request.getUsername()))
                .password(BCrypt.hashpw(request.getPassword()))
                .role(role)
                .pushChannel("feishu")
                .dailyPushLimit(10)
                .pushCooldownHours(0)
                .status(1)
                .build();
        return userProfileService.createUser(userProfile);
    }

    private void validateRegisterRequest(AuthRegisterRequest request) {
        if (request == null || StrUtil.hasBlank(request.getNickname(), request.getUsername(), request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
    }

    private UserProfile getUserByUsername(String username) {
        return userProfileService.getOne(QueryWrapper.create().eq("username", normalizeUsername(username)));
    }

    private String normalizeUsername(String username) {
        return StrUtil.trim(username).toLowerCase();
    }

    private String resolveRegisterRole() {
        return userProfileService.list().isEmpty() ? "admin" : "user";
    }

    private LoginUserVO toLoginUserVO(UserProfile userProfile, String token) {
        return LoginUserVO.builder()
                .id(userProfile.getId())
                .nickname(userProfile.getNickname())
                .username(userProfile.getUsername())
                .role(userProfile.getRole())
                .token(token)
                .build();
    }
}
