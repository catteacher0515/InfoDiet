package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.user.AdminUserSubscriptionVO;
import com.pingyu.infodiet.model.dto.user.UserListItemVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户信息接口
 */
@RestController
@RequestMapping("/user")
public class UserProfileController {

    @Resource
    private UserProfileService userProfileService;

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public BaseResponse<Long> createUser(@RequestBody UserProfile userProfile) {
        return ResultUtils.success(userProfileService.createUser(userProfile));
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserProfile userProfile) {
        return ResultUtils.success(userProfileService.updateUser(userProfile));
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/get/{userId}")
    public BaseResponse<UserProfile> getUserById(@PathVariable Long userId) {
        return ResultUtils.success(userProfileService.getUserById(userId));
    }

    /**
     * 查询启用用户
     */
    @GetMapping("/list/enabled")
    public BaseResponse<List<UserProfile>> listEnabledUsers() {
        return ResultUtils.success(userProfileService.listEnabledUsers());
    }

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public BaseResponse<List<UserListItemVO>> listUsers() {
        if (!"admin".equals(LoginUserContext.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(userProfileService.listUsers());
    }

    /**
     * 查询管理区用户订阅详情
     */
    @GetMapping("/admin/subscription/{userId}")
    public BaseResponse<AdminUserSubscriptionVO> getAdminUserSubscription(@PathVariable Long userId) {
        if (!"admin".equals(LoginUserContext.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(userProfileService.getAdminUserSubscription(userId));
    }
}
