package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.UserKeywordSubscriptionMapper;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户关键词订阅表 服务层实现。
 */
@Service
public class UserKeywordSubscriptionServiceImpl extends ServiceImpl<UserKeywordSubscriptionMapper, UserKeywordSubscription>
        implements UserKeywordSubscriptionService {

    /**
     * 添加关键词订阅
     */
    @Override
    public boolean addKeyword(Long userId, String keyword) {
        UserKeywordSubscription subscription = UserKeywordSubscription.builder()
                .userId(userId)
                .keyword(StrUtil.trim(keyword))
                .status(1)
                .build();
        return this.save(subscription);
    }

    /**
     * 删除关键词订阅
     */
    @Override
    public boolean removeKeyword(Long userId, String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("keyword", StrUtil.trim(keyword));
        return this.remove(queryWrapper);
    }

    /**
     * 查询用户关键词列表
     */
    @Override
    public List<String> listKeywordsByUserId(Long userId) {
        return listEnabledSubscriptionsByUserId(userId).stream()
                .map(UserKeywordSubscription::getKeyword)
                .toList();
    }

    /**
     * 查询启用订阅列表
     */
    @Override
    public List<UserKeywordSubscription> listEnabledSubscriptionsByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("status", 1);
        return this.list(queryWrapper);
    }
}
