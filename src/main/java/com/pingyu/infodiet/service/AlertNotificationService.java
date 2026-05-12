package com.pingyu.infodiet.service;

/**
 * 告警通知服务
 */
public interface AlertNotificationService {

    /**
     * 发送待处理告警到飞书
     */
    int sendPendingAlertsToFeishu();

    /**
     * 发送单条告警到飞书
     */
    boolean sendAlertToFeishu(Long alertId);
}
