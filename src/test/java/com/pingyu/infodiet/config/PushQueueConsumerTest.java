package com.pingyu.infodiet.config;

import com.pingyu.infodiet.service.PushQueueService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushQueueConsumerTest {

    @Test
    void handleRecordShouldDispatchPushMessage() {
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);

        PushQueueConsumer consumer = new PushQueueConsumer();
        ReflectionTestUtils.setField(consumer, "pushQueueService", pushQueueService);
        when(pushQueueService.handlePushMessage(
                new PushQueueService.PushMessage(1L, 11L, 101L, "feishu")
        )).thenReturn(true);

        Map<String, String> value = new LinkedHashMap<>();
        value.put("pushId", "1");
        value.put("userId", "11");
        value.put("contentItemId", "101");
        value.put("pushChannel", "feishu");
        MapRecord<String, Object, Object> record = MapRecord.create("info_diet:push:stream", (Map) value);

        consumer.handleRecord(record);

        verify(pushQueueService, times(1)).handlePushMessage(
                new PushQueueService.PushMessage(1L, 11L, 101L, "feishu")
        );
    }
}
