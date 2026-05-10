package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;

import java.util.List;

/**
 * 用户关键词订阅表 服务层。
 */
public interface UserKeywordSubscriptionService extends IService<UserKeywordSubscription> {

    /**
     * 添加关键词订阅
     */
    boolean addKeyword(Long userId, String keyword);

    /**
     * 删除关键词订阅
     */
    boolean removeKeyword(Long userId, String keyword);

    /**
     * 查询用户关键词列表
     */
    List<String> listKeywordsByUserId(Long userId);

    /**
     * 查询启用订阅列表
     */
    List<UserKeywordSubscription> listEnabledSubscriptionsByUserId(Long userId);
}
