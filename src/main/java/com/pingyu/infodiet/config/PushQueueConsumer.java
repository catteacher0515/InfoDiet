package com.pingyu.infodiet.config;

import com.pingyu.infodiet.service.PushQueueService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 推送消息消费者
 */
@Component
@Slf4j
public class PushQueueConsumer {

    @Resource
    private PushQueueService pushQueueService;

    /**
     * 消费 RabbitMQ 消息
     */
    @RabbitListener(queues = "#{infoDietProperties.pushQueueName}")
    public void receive(PushQueueService.PushMessage pushMessage) {
        if (pushMessage == null) {
            log.warn("收到空的推送消息");
            return;
        }
        pushQueueService.handlePushMessage(pushMessage);
    }

    /**
     * 处理单条消息
     */
    protected boolean handleMessage(PushQueueService.PushMessage pushMessage) {
        return pushQueueService.handlePushMessage(pushMessage);
    }
}
