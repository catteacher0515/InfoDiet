package com.pingyu.infodiet.model.dto.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * 运维端概览
 */
@Data
@Builder
public class OpsDashboardVO {

    /**
     * 最近任务数
     */
    private int recentTaskCount;

    /**
     * 待处理告警数
     */
    private int pendingAlertCount;

    /**
     * 失败推送数
     */
    private int failedPushCount;

    /**
     * 今日日报是否已生成
     */
    private Boolean todayDigestGenerated;

    /**
     * 今日日报推送成功数
     */
    private int todayDigestPushSuccessCount;

    /**
     * 今日日报推送失败数
     */
    private int todayDigestPushFailedCount;

    /**
     * 最近日报失败记录数
     */
    private int recentDigestFailedRecordCount;
}
