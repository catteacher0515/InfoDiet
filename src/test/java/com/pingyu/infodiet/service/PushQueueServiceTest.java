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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        when(userContentPushService.listEnqueueablePushesByChannel("feishu")).thenReturn(List.of(
                UserContentPush.builder().id(1L).userId(11L).contentItemId(101L).pushChannel("feishu").build(),
                UserContentPush.builder().id(2L).userId(12L).contentItemId(102L).pushChannel("feishu").build()
        ));
        when(userContentPushService.markQueued(1L)).thenReturn(true);
        when(userContentPushService.markQueued(2L)).thenReturn(true);

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);

        PushQueueService.EnqueuePushResult result = service.enqueuePendingPushes("feishu");

        assertEquals(2, result.totalCount());
        assertEquals(2, result.enqueuedCount());
        assertEquals(0, result.skippedCount());
        verify(streamOperations, times(2)).add(any(MapRecord.class));
        verify(userContentPushService, times(1)).markQueued(1L);
        verify(userContentPushService, times(1)).markQueued(2L);
    }

    @Test
    void enqueuePendingPushesShouldSkipWhenAlreadyQueued() {
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        StreamOperations<String, Object, Object> streamOperations = Mockito.mock(StreamOperations.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);

        when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);
        when(userContentPushService.listEnqueueablePushesByChannel("feishu")).thenReturn(List.of(
                UserContentPush.builder().id(1L).userId(11L).contentItemId(101L).pushChannel("feishu").build()
        ));
        when(userContentPushService.markQueued(1L)).thenReturn(false);

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);

        PushQueueService.EnqueuePushResult result = service.enqueuePendingPushes("feishu");

        assertEquals(1, result.totalCount());
        assertEquals(0, result.enqueuedCount());
        assertEquals(1, result.skippedCount());
        verify(streamOperations, times(0)).add(any(MapRecord.class));
    }

    @Test
    void handlePushMessageShouldInvokeSinglePush() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        when(userContentPushService.markConsuming(1L)).thenReturn(true);
        when(feishuPushService.pushSingleUserContentItemToFeishu(1L)).thenReturn(true);

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);

        boolean result = service.handlePushMessage(new PushQueueService.PushMessage(1L, 11L, 101L, "feishu"));

        assertFalse(!result);
        verify(feishuPushService, times(1)).pushSingleUserContentItemToFeishu(1L);
        verify(userContentPushService, times(1)).markConsuming(1L);
    }

    @Test
    void handlePushMessageShouldSkipWhenRecordIsNotEnqueued() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        when(userContentPushService.markConsuming(1L)).thenReturn(false);

        PushQueueServiceImpl service = new PushQueueServiceImpl();
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);

        boolean result = service.handlePushMessage(new PushQueueService.PushMessage(1L, 11L, 101L, "feishu"));

        assertFalse(result);
        verify(feishuPushService, times(0)).pushSingleUserContentItemToFeishu(eq(1L));
    }
}
