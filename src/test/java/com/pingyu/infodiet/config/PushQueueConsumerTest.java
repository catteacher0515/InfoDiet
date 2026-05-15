package com.pingyu.infodiet.config;

import com.pingyu.infodiet.service.PushQueueService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.times;
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
}
