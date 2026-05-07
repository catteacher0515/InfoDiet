package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 飞书推送服务
 */
public interface FeishuPushService {

    /**
     * 查询待推送内容
     */
    List<ContentItem> listPendingPushItems();

    /**
     * 推送内容到飞书
     */
    PushResult pushContentItemsToFeishu();

    /**
     * 批量更新推送状态
     */
    boolean markItemsAsPushed(List<Long> itemIds);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class PushResult {

        /**
         * 本次处理总数
         */
        private int totalCount;

        /**
         * 推送成功数
         */
        private int successCount;

        /**
         * 推送失败数
         */
        private int failedCount;
    }
}
