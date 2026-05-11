package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.UserSourceSubscriptionMapper;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户订阅源表 服务层实现。
 */
@Service
public class UserSourceSubscriptionServiceImpl
        extends ServiceImpl<UserSourceSubscriptionMapper, UserSourceSubscription>
        implements UserSourceSubscriptionService {

    /**
     * 添加订阅源
     */
    @Override
    @CacheEvict(
            cacheNames = {"enabledSourceSubscriptions", "matchEnabledUsersWithDetails", "unifiedContentItems"},
            allEntries = true
    )
    public boolean addSourceSubscription(UserSourceSubscription userSourceSubscription) {
        userSourceSubscription.setPlatform(normalize(userSourceSubscription.getPlatform()));
        userSourceSubscription.setSourceType(normalize(userSourceSubscription.getSourceType()));
        userSourceSubscription.setSourceValue(StrUtil.trim(userSourceSubscription.getSourceValue()));
        if (userSourceSubscription.getStatus() == null) {
            userSourceSubscription.setStatus(1);
        }
        return this.save(userSourceSubscription);
    }

    /**
     * 查询启用订阅源列表
     */
    @Override
    @Cacheable(cacheNames = "enabledSourceSubscriptions", key = "'all'")
    public List<UserSourceSubscription> listEnabledSourceSubscriptions() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", 1);
        return this.list(queryWrapper);
    }

    /**
     * 标准化文本
     */
    protected String normalize(String value) {
        return StrUtil.trim(value).toLowerCase();
    }
}
