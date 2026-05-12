package com.pingyu.infodiet.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.model.dto.user.AdminUserSubscriptionVO;
import com.pingyu.infodiet.mapper.UserProfileMapper;
import com.pingyu.infodiet.model.dto.user.UserListItemVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserProfileService;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表 服务层实现。
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    /**
     * 创建用户
     */
    @Override
    @CacheEvict(cacheNames = "enabledUsers", allEntries = true)
    public Long createUser(UserProfile userProfile) {
        this.save(userProfile);
        return userProfile.getId();
    }

    /**
     * 更新用户
     */
    @Override
    @CacheEvict(cacheNames = {"enabledUsers", "matchEnabledUsersWithDetails"}, allEntries = true)
    public boolean updateUser(UserProfile userProfile) {
        if (userProfile == null || userProfile.getId() == null) {
            return false;
        }
        UserProfile existingUser = this.getById(userProfile.getId());
        if (existingUser == null) {
            return false;
        }
        if (userProfile.getNickname() != null) {
            existingUser.setNickname(userProfile.getNickname());
        }
        if (userProfile.getUsername() != null) {
            existingUser.setUsername(userProfile.getUsername());
        }
        if (userProfile.getPassword() != null) {
            existingUser.setPassword(userProfile.getPassword());
        }
        if (userProfile.getRole() != null) {
            existingUser.setRole(userProfile.getRole());
        }
        if (userProfile.getFeishuUserId() != null) {
            existingUser.setFeishuUserId(userProfile.getFeishuUserId());
        }
        if (userProfile.getPushChannel() != null) {
            existingUser.setPushChannel(userProfile.getPushChannel());
        }
        if (userProfile.getDailyPushLimit() != null) {
            existingUser.setDailyPushLimit(userProfile.getDailyPushLimit());
        }
        if (userProfile.getPushCooldownHours() != null) {
            existingUser.setPushCooldownHours(userProfile.getPushCooldownHours());
        }
        if (userProfile.getStatus() != null) {
            existingUser.setStatus(userProfile.getStatus());
        }
        return this.updateById(existingUser);
    }

    /**
     * 根据主键获取用户
     */
    @Override
    public UserProfile getUserById(Long userId) {
        return this.getById(userId);
    }

    /**
     * 查询启用用户
     */
    @Override
    @Cacheable(cacheNames = "enabledUsers", key = "'all'")
    public List<UserProfile> listEnabledUsers() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", 1);
        return this.list(queryWrapper);
    }

    /**
     * 查询用户列表
     */
    @Override
    public List<UserListItemVO> listUsers() {
        return this.list().stream()
                .map(this::toUserListItem)
                .toList();
    }

    /**
     * 查询用户列表项
     */
    @Override
    public UserListItemVO getUserListItemById(Long userId) {
        UserProfile userProfile = this.getById(userId);
        if (userProfile == null) {
            return null;
        }
        return toUserListItem(userProfile);
    }

    /**
     * 查询管理区用户订阅详情
     */
    @Override
    public AdminUserSubscriptionVO getAdminUserSubscription(Long userId) {
        return AdminUserSubscriptionVO.builder()
                .user(getUserListItemById(userId))
                .keywords(userKeywordSubscriptionService.listKeywordsByUserId(userId))
                .rules(userSubscriptionRuleService.listEnabledRulesByUserId(userId))
                .sources(userSourceSubscriptionService.listEnabledSourceSubscriptions().stream()
                        .filter(item -> userId.equals(item.getUserId()))
                        .toList())
                .build();
    }

    protected UserListItemVO toUserListItem(UserProfile userProfile) {
        return UserListItemVO.builder()
                .id(userProfile.getId())
                .nickname(userProfile.getNickname())
                .username(userProfile.getUsername())
                .role(userProfile.getRole())
                .status(userProfile.getStatus())
                .build();
    }
}
