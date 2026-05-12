package com.pingyu.infodiet.model.dto.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * 管理端概览
 */
@Data
@Builder
public class AdminDashboardVO {

    /**
     * 用户总数
     */
    private int userCount;

    /**
     * 启用用户数
     */
    private int enabledUserCount;

    /**
     * 关键词订阅数
     */
    private int keywordSubscriptionCount;

    /**
     * 订阅源数
     */
    private int sourceSubscriptionCount;
}
