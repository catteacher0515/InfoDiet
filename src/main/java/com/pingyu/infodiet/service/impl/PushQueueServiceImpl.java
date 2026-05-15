package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.FeishuPushService;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 推送消息队列服务实现
 */
@Service
@Slf4j
public class PushQueueServiceImpl implements PushQueueService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private FeishuPushService feishuPushService;

    @Resource
    private InfoDietProperties infoDietProperties;

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
            try {
                rabbitTemplate.convertAndSend(infoDietProperties.getPushQueueName(), buildMessage(pendingPush));
                enqueuedCount++;
            } catch (Exception e) {
                log.error("RabbitMQ 消息入队失败，pushId={}", pendingPush.getId(), e);
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
    protected PushMessage buildMessage(UserContentPush pendingPush) {
        return new PushMessage(
                pendingPush.getId(),
                pendingPush.getUserId(),
                pendingPush.getContentItemId(),
                StrUtil.blankToDefault(pendingPush.getPushChannel(), "")
        );
    }
}
