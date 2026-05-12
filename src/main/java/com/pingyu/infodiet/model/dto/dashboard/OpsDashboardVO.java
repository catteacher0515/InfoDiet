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
}
