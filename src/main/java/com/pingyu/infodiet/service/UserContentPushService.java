package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.UserContentPush;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户内容推送表 服务层。
 */
public interface UserContentPushService extends IService<UserContentPush> {

    /**
     * 生成待推送记录
     */
    CreatePushResult createPendingPushes();

    /**
     * 查询可入队推送记录
     */
    List<UserContentPush> listEnqueueablePushesByChannel(String pushChannel);

    /**
     * 标记已入队
     */
    boolean markQueued(Long pushId);

    /**
     * 标记消费中
     */
    boolean markConsuming(Long pushId);

    /**
     * 标记推送成功
     */
    boolean markPushSuccess(Long pushId);

    /**
     * 标记推送失败
     */
    boolean markPushFailed(Long pushId, String failReason);

    /**
     * 重试失败推送
     */
    boolean retryFailedPush(Long pushId);

    /**
     * 查询失败推送列表
     */
    List<UserContentPush> listFailedPushesByChannel(String pushChannel);

    /**
     * 查询当前用户推送记录
     */
    List<UserContentPush> listPushesByUserId(Long userId);

    /**
     * 批量重试失败推送
     */
    BatchRetryResult retryFailedPushes(List<Long> pushIdList);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class CreatePushResult {

        /**
         * 本次处理总数
         */
        private int totalCount;

        /**
         * 实际新增数
         */
        private int createdCount;

        /**
         * 跳过数
         */
        private int skippedCount;

        /**
         * 因已存在跳过数
         */
        private int skippedByExistingCount;

        /**
         * 因超上限跳过数
         */
        private int skippedByLimitCount;

        /**
         * 因冷却期跳过数
         */
        private int skippedByCooldownCount;

        public CreatePushResult(int totalCount, int createdCount, int skippedCount) {
            this(totalCount, createdCount, skippedCount, 0, 0, 0);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class BatchRetryResult {

        /**
         * 总数
         */
        private int totalCount;

        /**
         * 成功数
         */
        private int successCount;

        /**
         * 失败数
         */
        private int failedCount;
    }

}
