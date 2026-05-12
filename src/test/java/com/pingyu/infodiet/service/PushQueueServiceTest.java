package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.impl.PushQueueServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushQueueServiceTest {

    @Test
    void enqueuePendingPushesShouldPublishAllPendingPushRecords() {
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        StreamOperations<String, Object, Object> streamOperations = Mockito.mock(StreamOperations.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);

        when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.add(any(MapRecord.class))).thenReturn(RecordId.of("1-0"));
        when(userContentPushService.listPendingPushesByChannel("feishu")).thenReturn(List.of(
                UserContentPush.builder().id(1L).userId(11L).contentItemId(101L).pushChannel("feishu").build(),
                UserContentPush.builder().id(2L).userId(12L).contentItemId(102L).pushChannel("feishu").build()
        ));

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);

        PushQueueService.EnqueuePushResult result = service.enqueuePendingPushes("feishu");

        assertEquals(2, result.totalCount());
        assertEquals(2, result.enqueuedCount());
        assertEquals(0, result.skippedCount());
        verify(streamOperations, times(2)).add(any(MapRecord.class));
    }

    @Test
    void handlePushMessageShouldInvokeSinglePush() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);

        service.handlePushMessage(new PushQueueService.PushMessage(1L, 11L, 101L, "feishu"));

        verify(feishuPushService, times(1)).pushSingleUserContentItemToFeishu(1L);
    }
}
