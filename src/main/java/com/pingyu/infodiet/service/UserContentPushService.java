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
     * 查询待推送记录
     */
    List<UserContentPush> listPendingPushesByChannel(String pushChannel);

    /**
     * 标记推送成功
     */
    boolean markPushSuccess(Long pushId);

    /**
     * 标记推送失败
     */
    boolean markPushFailed(Long pushId, String failReason);

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

}
