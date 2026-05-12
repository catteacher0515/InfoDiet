package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.dashboard.AdminSubscriptionOverviewVO;

/**
 * 管理区总览服务
 */
public interface AdminOverviewService {

    /**
     * 查询订阅总览
     */
    AdminSubscriptionOverviewVO getSubscriptionOverview();
}
