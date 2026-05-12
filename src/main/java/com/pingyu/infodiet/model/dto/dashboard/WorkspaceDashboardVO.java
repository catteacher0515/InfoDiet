package com.pingyu.infodiet.model.dto.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * 用户工作台概览
 */
@Data
@Builder
public class WorkspaceDashboardVO {

    /**
     * 关键词数
     */
    private int keywordCount;

    /**
     * 订阅源数
     */
    private int sourceCount;

    /**
     * 总推送数
     */
    private int totalPushCount;

    /**
     * 成功推送数
     */
    private int successPushCount;

    /**
     * 失败推送数
     */
    private int failedPushCount;
}
