package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.AlertRecord;

import java.util.List;

/**
 * 失败告警记录服务
 */
public interface AlertRecordService extends IService<AlertRecord> {

    /**
     * 创建或更新告警记录
     */
    AlertRecord createOrUpdateAlert(
            String alertType,
            String alertLevel,
            String sourceType,
            Long sourceId,
            String alertTitle,
            String alertContent
    );

    /**
     * 标记告警已发送
     */
    boolean markAlertSent(Long alertId);

    /**
     * 标记告警发送失败
     */
    boolean markAlertSendFailed(Long alertId, String failReason);

    /**
     * 查询待处理告警
     */
    List<AlertRecord> listPendingAlerts();

    /**
     * 查询来源关联告警
     */
    AlertRecord getBySource(String sourceType, Long sourceId);
}
