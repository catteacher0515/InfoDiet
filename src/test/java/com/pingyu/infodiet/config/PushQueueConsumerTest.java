package com.pingyu.infodiet.config;

import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushQueueConsumerTest {

    @Test
    void handleMessageShouldDispatchPushMessage() {
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);

        PushQueueConsumer consumer = new PushQueueConsumer();
        ReflectionTestUtils.setField(consumer, "pushQueueService", pushQueueService);
        when(pushQueueService.handlePushMessage(
                new PushQueueService.PushMessage(1L, 11L, 101L, "feishu")
        )).thenReturn(true);

        consumer.handleMessage(new PushQueueService.PushMessage(1L, 11L, 101L, "feishu"));

        verify(pushQueueService, times(1)).handlePushMessage(
                new PushQueueService.PushMessage(1L, 11L, 101L, "feishu")
        );
    }

    @Test
    void handleMessageShouldMarkFailedAndCreateAlertWhenConsumeThrowsException() {
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);

        PushQueueConsumer consumer = new PushQueueConsumer();
        ReflectionTestUtils.setField(consumer, "pushQueueService", pushQueueService);
        ReflectionTestUtils.setField(consumer, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(consumer, "alertRecordService", alertRecordService);

        PushQueueService.PushMessage pushMessage = new PushQueueService.PushMessage(1L, 11L, 101L, "feishu");
        when(pushQueueService.handlePushMessage(pushMessage)).thenThrow(new RuntimeException("mock consume error"));

        consumer.handleMessage(pushMessage);

        verify(userContentPushService, times(1))
                .markPushFailed(1L, "RabbitMQ 消费异常: mock consume error");
        verify(alertRecordService, times(1)).createOrUpdateAlert(
                "push_consume_failed",
                "error",
                "user_content_push",
                1L,
                "RabbitMQ 消费失败",
                "pushId=1, pushChannel=feishu, reason=RabbitMQ 消费异常: mock consume error"
        );
        verifyNoMoreInteractions(userContentPushService);
    }
}
