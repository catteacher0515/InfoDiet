package com.pingyu.infodiet.model.dto.user;

import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理区用户订阅详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSubscriptionVO {

    /**
     * 用户信息
     */
    private UserListItemVO user;

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
