package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;

import java.util.List;
import java.util.Map;

/**
 * 订阅匹配服务
 */
public interface SubscriptionMatchService {

    /**
     * 匹配启用用户的订阅内容
     */
    Map<Long, List<ContentItem>> matchEnabledUsers();
}
