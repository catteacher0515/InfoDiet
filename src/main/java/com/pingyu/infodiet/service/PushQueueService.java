package com.pingyu.infodiet.service;

import java.io.Serializable;

/**
 * 推送消息队列服务
 */
public interface PushQueueService {

    /**
     * 入队结果
     */
    record EnqueuePushResult(int totalCount, int enqueuedCount, int skippedCount) {
    }

    /**
     * 推送消息
     */
    record PushMessage(Long pushId, Long userId, Long contentItemId, String pushChannel) implements Serializable {
    }

    /**
     * 将待推送记录加入消息队列
     */
    EnqueuePushResult enqueuePendingPushes(String pushChannel);

    /**
     * 处理单条推送消息
     */
    boolean handlePushMessage(PushMessage pushMessage);
}
