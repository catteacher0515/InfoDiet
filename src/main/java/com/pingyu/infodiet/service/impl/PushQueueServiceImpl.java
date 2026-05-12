package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.FeishuPushService;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送消息队列服务实现
 */
@Service
@Slf4j
public class PushQueueServiceImpl implements PushQueueService {

    public static final String PUSH_STREAM_KEY = "info_diet:push:stream";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private FeishuPushService feishuPushService;

    /**
     * 将待推送记录加入消息队列
     */
    @Override
    public EnqueuePushResult enqueuePendingPushes(String pushChannel) {
        List<UserContentPush> pendingPushes = userContentPushService.listEnqueueablePushesByChannel(pushChannel);
        if (CollUtil.isEmpty(pendingPushes)) {
            return new EnqueuePushResult(0, 0, 0);
        }
        int enqueuedCount = 0;
        int skippedCount = 0;
        for (UserContentPush pendingPush : pendingPushes) {
            boolean queued = userContentPushService.markQueued(pendingPush.getId());
            if (!queued) {
                skippedCount++;
                continue;
            }
            RecordId recordId = stringRedisTemplate.opsForStream().add(buildRecord(pendingPush));
            if (recordId != null) {
                enqueuedCount++;
            } else {
                userContentPushService.markPushFailed(pendingPush.getId(), "消息入队失败");
                skippedCount++;
            }
        }
        return new EnqueuePushResult(pendingPushes.size(), enqueuedCount, skippedCount);
    }

    /**
     * 处理单条推送消息
     */
    @Override
    public boolean handlePushMessage(PushMessage pushMessage) {
        if (pushMessage == null || pushMessage.pushId() == null || StrUtil.isBlank(pushMessage.pushChannel())) {
            return false;
        }
        boolean consuming = userContentPushService.markConsuming(pushMessage.pushId());
        if (!consuming) {
            return false;
        }
        if (StrUtil.equalsIgnoreCase(pushMessage.pushChannel(), "feishu")) {
            return feishuPushService.pushSingleUserContentItemToFeishu(pushMessage.pushId());
        }
        log.warn("暂不支持的推送渠道，pushId={}, pushChannel={}", pushMessage.pushId(), pushMessage.pushChannel());
        userContentPushService.markPushFailed(pushMessage.pushId(), "暂不支持的推送渠道");
        return false;
    }

    /**
     * 构建队列消息
     */
    protected StringRecord buildRecord(UserContentPush pendingPush) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("pushId", String.valueOf(pendingPush.getId()));
        message.put("userId", String.valueOf(pendingPush.getUserId()));
        message.put("contentItemId", String.valueOf(pendingPush.getContentItemId()));
        message.put("pushChannel", StrUtil.blankToDefault(pendingPush.getPushChannel(), ""));
        return StreamRecords.string(message).withStreamKey(PUSH_STREAM_KEY);
    }
}
