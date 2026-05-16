package com.pingyu.infodiet.config;

import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
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

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private AlertRecordService alertRecordService;

    /**
     * 消费 RabbitMQ 消息
     */
    @RabbitListener(queues = "#{infoDietProperties.pushQueueName}")
    public void receive(PushQueueService.PushMessage pushMessage) {
        if (pushMessage == null) {
            log.warn("收到空的推送消息");
            return;
        }
        handleMessage(pushMessage);
    }

    /**
     * 处理单条消息
     */
    protected boolean handleMessage(PushQueueService.PushMessage pushMessage) {
        try {
            return pushQueueService.handlePushMessage(pushMessage);
        } catch (Exception e) {
            log.error("RabbitMQ 消费异常，pushId={}", pushMessage.pushId(), e);
            handleConsumeException(pushMessage, e);
            return false;
        }
    }

    /**
     * 处理消费异常
     */
    protected void handleConsumeException(PushQueueService.PushMessage pushMessage, Exception e) {
        if (pushMessage == null || pushMessage.pushId() == null) {
            return;
        }
        String failReason = buildFailReason(e);
        userContentPushService.markPushFailed(pushMessage.pushId(), failReason);
        alertRecordService.createOrUpdateAlert(
                "push_consume_failed",
                "error",
                "user_content_push",
                pushMessage.pushId(),
                "RabbitMQ 消费失败",
                "pushId=" + pushMessage.pushId()
                        + ", pushChannel=" + pushMessage.pushChannel()
                        + ", reason=" + failReason
        );
    }

    /**
     * 构建失败原因
     */
    protected String buildFailReason(Exception e) {
        String message = e == null || e.getMessage() == null || e.getMessage().isBlank()
                ? "未知异常"
                : e.getMessage();
        return "RabbitMQ 消费异常: " + message;
    }
}
