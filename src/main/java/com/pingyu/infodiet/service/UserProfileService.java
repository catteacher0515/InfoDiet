package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.UserProfile;

import java.util.List;

/**
 * 用户信息表 服务层。
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 创建用户
     */
    Long createUser(UserProfile userProfile);

    /**
     * 更新用户
     */
    boolean updateUser(UserProfile userProfile);

    /**
     * 根据主键获取用户
     */
    UserProfile getUserById(Long userId);

    /**
     * 查询启用用户
     */
    List<UserProfile> listEnabledUsers();
}
