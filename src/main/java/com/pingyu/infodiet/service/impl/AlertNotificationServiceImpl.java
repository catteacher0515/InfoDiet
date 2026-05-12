package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.AlertNotificationService;
import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.UserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 告警通知服务实现
 */
@Service
@Slf4j
public class AlertNotificationServiceImpl implements AlertNotificationService {

    @Resource
    private AlertRecordService alertRecordService;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private FeishuBaseProperties feishuBaseProperties;

    /**
     * 发送待处理告警到飞书
     */
    @Override
    public int sendPendingAlertsToFeishu() {
        List<AlertRecord> pendingAlerts = alertRecordService.listPendingAlerts();
        if (CollUtil.isEmpty(pendingAlerts)) {
            return 0;
        }
        int successCount = 0;
        for (AlertRecord pendingAlert : pendingAlerts) {
            if (sendAlertToFeishu(pendingAlert.getId())) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 发送单条告警到飞书
     */
    @Override
    public boolean sendAlertToFeishu(Long alertId) {
        if (alertId == null) {
            return false;
        }
        AlertRecord alertRecord = alertRecordService.getById(alertId);
        if (alertRecord == null) {
            return false;
        }
        List<UserProfile> enabledUsers = userProfileService.listEnabledUsers();
        List<UserProfile> feishuUsers = enabledUsers.stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getPushChannel(), "feishu"))
                .filter(item -> StrUtil.isNotBlank(item.getFeishuUserId()))
                .toList();
        if (CollUtil.isEmpty(feishuUsers)) {
            alertRecordService.markAlertSendFailed(alertId, "未找到可用的飞书告警接收人");
            return false;
        }
        try {
            Client client = buildClient();
            String content = buildTextContent(alertRecord);
            for (UserProfile feishuUser : feishuUsers) {
                CreateMessageReq req = CreateMessageReq.newBuilder()
                        .receiveIdType("open_id")
                        .createMessageReqBody(CreateMessageReqBody.newBuilder()
                                .receiveId(feishuUser.getFeishuUserId())
                                .msgType("text")
                                .content(content)
                                .uuid(UUID.randomUUID().toString())
                                .build())
                        .build();
                CreateMessageResp resp = client.im().message().create(req);
                if (resp == null || !resp.success()) {
                    String failReason = resp == null
                            ? "飞书告警发送失败，响应为空"
                            : "code=" + resp.getCode() + ",msg=" + StrUtil.blankToDefault(resp.getMsg(), "");
                    alertRecordService.markAlertSendFailed(alertId, failReason);
                    return false;
                }
            }
            alertRecordService.markAlertSent(alertId);
            return true;
        } catch (Exception e) {
            log.error("飞书告警发送异常，alertId={}", alertId, e);
            alertRecordService.markAlertSendFailed(alertId, e.getMessage());
            return false;
        }
    }

    /**
     * 构建飞书客户端
     */
    protected Client buildClient() {
        return Client.newBuilder(
                feishuBaseProperties.getAppId(),
                feishuBaseProperties.getAppSecret()
        ).build();
    }

    /**
     * 构建文本消息内容
     */
    protected String buildTextContent(AlertRecord alertRecord) {
        Map<String, String> content = new LinkedHashMap<>();
        content.put("text", "【信息节食失败告警】\n"
                + "类型：" + StrUtil.blankToDefault(alertRecord.getAlertType(), "") + "\n"
                + "级别：" + StrUtil.blankToDefault(alertRecord.getAlertLevel(), "") + "\n"
                + "标题：" + StrUtil.blankToDefault(alertRecord.getAlertTitle(), "") + "\n"
                + "内容：" + StrUtil.blankToDefault(alertRecord.getAlertContent(), ""));
        return Jsons.DEFAULT.toJson(content);
    }

    /**
     * 获取当前时间
     */
    protected java.time.LocalDateTime now() {
        return java.time.LocalDateTime.now();
    }
}
