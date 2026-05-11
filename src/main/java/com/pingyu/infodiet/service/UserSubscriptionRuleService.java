package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;

import java.util.List;

/**
 * 用户订阅规则表 服务层。
 */
public interface UserSubscriptionRuleService extends IService<UserSubscriptionRule> {

    /**
     * 添加订阅规则
     */
    boolean addRule(UserSubscriptionRule userSubscriptionRule);

    /**
     * 删除订阅规则
     */
    boolean removeRule(Long userId, String ruleType, String ruleValue);

    /**
     * 查询启用规则列表
     */
    List<UserSubscriptionRule> listEnabledRulesByUserId(Long userId);
}
