package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.UserSubscriptionRuleMapper;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户订阅规则表 服务层实现。
 */
@Service
public class UserSubscriptionRuleServiceImpl extends ServiceImpl<UserSubscriptionRuleMapper, UserSubscriptionRule>
        implements UserSubscriptionRuleService {

    /**
     * 添加订阅规则
     */
    @Override
    public boolean addRule(UserSubscriptionRule userSubscriptionRule) {
        userSubscriptionRule.setRuleType(StrUtil.trim(userSubscriptionRule.getRuleType()));
        userSubscriptionRule.setRuleValue(StrUtil.trim(userSubscriptionRule.getRuleValue()));
        if (userSubscriptionRule.getRuleWeight() == null) {
            userSubscriptionRule.setRuleWeight(1);
        }
        if (userSubscriptionRule.getStatus() == null) {
            userSubscriptionRule.setStatus(1);
        }
        return this.save(userSubscriptionRule);
    }

    /**
     * 删除订阅规则
     */
    @Override
    public boolean removeRule(Long userId, String ruleType, String ruleValue) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("ruleType", StrUtil.trim(ruleType))
                .eq("ruleValue", StrUtil.trim(ruleValue));
        return this.remove(queryWrapper);
    }

    /**
     * 查询启用规则列表
     */
    @Override
    public List<UserSubscriptionRule> listEnabledRulesByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("status", 1);
        return this.list(queryWrapper);
    }
}
