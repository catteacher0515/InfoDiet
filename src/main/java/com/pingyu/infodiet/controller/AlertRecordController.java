package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.service.AlertNotificationService;
import com.pingyu.infodiet.service.AlertRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 失败告警接口
 */
@RestController
@RequestMapping("/alert-record")
public class AlertRecordController {

    @Resource
    private AlertRecordService alertRecordService;

    @Resource
    private AlertNotificationService alertNotificationService;

    /**
     * 查询待处理告警
     */
    @GetMapping("/pending")
    public BaseResponse<List<AlertRecord>> listPendingAlerts() {
        return ResultUtils.success(alertRecordService.listPendingAlerts());
    }

    /**
     * 手动标记告警已发送
     */
    @PostMapping("/mark-sent")
    public BaseResponse<Boolean> markAlertSent(@RequestParam Long alertId) {
        return ResultUtils.success(alertRecordService.markAlertSent(alertId));
    }

    /**
     * 批量发送待处理告警到飞书
     */
    @PostMapping("/send/pending")
    public BaseResponse<Integer> sendPendingAlertsToFeishu() {
        return ResultUtils.success(alertNotificationService.sendPendingAlertsToFeishu());
    }

    /**
     * 发送单条告警到飞书
     */
    @PostMapping("/send")
    public BaseResponse<Boolean> sendAlertToFeishu(@RequestParam Long alertId) {
        return ResultUtils.success(alertNotificationService.sendAlertToFeishu(alertId));
    }
}
