package com.pingyu.infodiet.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.AlertRecordMapper;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.service.AlertRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 失败告警记录服务实现
 */
@Service
public class AlertRecordServiceImpl extends ServiceImpl<AlertRecordMapper, AlertRecord>
        implements AlertRecordService {

    private static final int FAIL_REASON_MAX_LENGTH = 512;

    /**
     * 创建或更新告警记录
     */
    @Override
    public AlertRecord createOrUpdateAlert(
            String alertType,
            String alertLevel,
            String sourceType,
            Long sourceId,
            String alertTitle,
            String alertContent
    ) {
        AlertRecord existingAlert = getExistingAlert(alertType, sourceType, sourceId);
        LocalDateTime now = now();
        if (existingAlert != null) {
            AlertRecord updateRecord = new AlertRecord();
            updateRecord.setId(existingAlert.getId());
            updateRecord.setAlertLevel(alertLevel);
            updateRecord.setAlertTitle(alertTitle);
            updateRecord.setAlertContent(alertContent);
            updateRecord.setAlertStatus(0);
            updateRecord.setFailReason(null);
            updateRecord.setSendTime(null);
            updateRecord.setLastOccurTime(now);
            this.updateById(updateRecord);
            existingAlert.setAlertLevel(alertLevel);
            existingAlert.setAlertTitle(alertTitle);
            existingAlert.setAlertContent(alertContent);
            existingAlert.setAlertStatus(0);
            existingAlert.setFailReason(null);
            existingAlert.setSendTime(null);
            existingAlert.setLastOccurTime(now);
            return existingAlert;
        }
        AlertRecord alertRecord = AlertRecord.builder()
                .alertType(alertType)
                .alertLevel(alertLevel)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .alertStatus(0)
                .alertTitle(alertTitle)
                .alertContent(alertContent)
                .firstOccurTime(now)
                .lastOccurTime(now)
                .build();
        this.save(alertRecord);
        return alertRecord;
    }

    /**
     * 标记告警已发送
     */
    @Override
    public boolean markAlertSent(Long alertId) {
        return this.updateChain()
                .set("alertStatus", 1)
                .set("sendTime", now())
                .set("failReason", null)
                .where("id = ?", alertId)
                .update();
    }

    /**
     * 标记告警发送失败
     */
    @Override
    public boolean markAlertSendFailed(Long alertId, String failReason) {
        return this.updateChain()
                .set("alertStatus", 2)
                .set("failReason", trimFailReason(failReason))
                .where("id = ?", alertId)
                .update();
    }

    /**
     * 查询待处理告警
     */
    @Override
    public List<AlertRecord> listPendingAlerts() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("alertStatus", 0)
                .orderBy("lastOccurTime", false);
        return this.list(queryWrapper);
    }

    /**
     * 查询已存在告警
     */
    protected AlertRecord getExistingAlert(String alertType, String sourceType, Long sourceId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("alertType", alertType)
                .eq("sourceType", sourceType)
                .eq("sourceId", sourceId);
        return this.getOne(queryWrapper);
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 截断失败原因，避免超出字段长度
     */
    protected String trimFailReason(String failReason) {
        if (failReason == null) {
            return null;
        }
        if (failReason.length() <= FAIL_REASON_MAX_LENGTH) {
            return failReason;
        }
        return failReason.substring(0, FAIL_REASON_MAX_LENGTH);
    }
}
