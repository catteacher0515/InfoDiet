package com.pingyu.infodiet.model.dto.user;

import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 我的订阅聚合视图
 */
@Data
@Builder
public class WorkspaceSubscriptionsVO {

    /**
     * 关键词列表
     */
    private List<String> keywords;

    /**
     * 规则列表
     */
    private List<UserSubscriptionRule> rules;

    /**
     * 订阅源列表
     */
    private List<UserSourceSubscription> sources;
}
