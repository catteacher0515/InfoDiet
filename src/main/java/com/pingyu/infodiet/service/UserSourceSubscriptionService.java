package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;

import java.util.List;

/**
 * 用户订阅源表 服务层。
 */
public interface UserSourceSubscriptionService extends IService<UserSourceSubscription> {

    /**
     * 添加订阅源
     */
    boolean addSourceSubscription(UserSourceSubscription userSourceSubscription);

    /**
     * 查询启用订阅源列表
     */
    List<UserSourceSubscription> listEnabledSourceSubscriptions();
}
