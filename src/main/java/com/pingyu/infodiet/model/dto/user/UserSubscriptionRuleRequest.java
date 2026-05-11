package com.pingyu.infodiet.model.dto.user;

import lombok.Data;

/**
 * 用户订阅规则请求
 */
@Data
public class UserSubscriptionRuleRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则值
     */
    private String ruleValue;

    /**
     * 规则权重
     */
    private Integer ruleWeight;
}
