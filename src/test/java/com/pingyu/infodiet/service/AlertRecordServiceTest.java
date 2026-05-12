package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.service.impl.AlertRecordServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertRecordServiceTest {

    @Test
    void createOrUpdateAlertShouldCreateNewRecordWhenNotExists() {
        InMemoryAlertRecordService service = new InMemoryAlertRecordService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);

        AlertRecord alertRecord = service.createOrUpdateAlert(
                "push_final_failed",
                "error",
                "user_content_push",
                101L,
                "推送最终失败",
                "pushId=101 已达到最大重试次数"
        );

        assertNotNull(alertRecord.getId());
        assertEquals(1, service.savedItems.size());
        assertEquals(0, alertRecord.getAlertStatus());
        assertEquals(service.fixedNow, alertRecord.getFirstOccurTime());
        assertEquals(service.fixedNow, alertRecord.getLastOccurTime());
    }

    @Test
    void createOrUpdateAlertShouldMergeWhenSameSourceExists() {
        InMemoryAlertRecordService service = new InMemoryAlertRecordService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(AlertRecord.builder()
                .id(1L)
                .alertType("push_final_failed")
                .alertLevel("error")
                .sourceType("user_content_push")
                .sourceId(101L)
                .alertStatus(0)
                .alertTitle("推送最终失败")
                .alertContent("旧内容")
                .firstOccurTime(service.fixedNow.minusMinutes(10))
                .lastOccurTime(service.fixedNow.minusMinutes(10))
                .build());
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 5);

        AlertRecord alertRecord = service.createOrUpdateAlert(
                "push_final_failed",
                "error",
                "user_content_push",
                101L,
                "推送最终失败",
                "新内容"
        );

        assertEquals(1, service.savedItems.size());
        assertEquals(1L, alertRecord.getId());
        assertEquals("新内容", alertRecord.getAlertContent());
        assertEquals(LocalDateTime.of(2026, 5, 12, 9, 50), alertRecord.getFirstOccurTime());
        assertEquals(service.fixedNow, alertRecord.getLastOccurTime());
    }

    @Test
    void markAlertSentShouldUpdateStatusAndSendTime() {
        InMemoryAlertRecordService service = new InMemoryAlertRecordService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(AlertRecord.builder()
                .id(1L)
                .alertStatus(0)
                .build());

        boolean result = service.markAlertSent(1L);

        assertTrue(result);
        assertEquals(1, service.savedItems.getFirst().getAlertStatus());
        assertEquals(service.fixedNow, service.savedItems.getFirst().getSendTime());
        assertNull(service.savedItems.getFirst().getFailReason());
    }

    @Test
    void markAlertSendFailedShouldUpdateFailureStatus() {
        InMemoryAlertRecordService service = new InMemoryAlertRecordService();
        service.savedItems.add(AlertRecord.builder()
                .id(1L)
                .alertStatus(0)
                .build());

        boolean result = service.markAlertSendFailed(1L, "飞书告警发送失败");

        assertTrue(result);
        assertEquals(2, service.savedItems.getFirst().getAlertStatus());
        assertEquals("飞书告警发送失败", service.savedItems.getFirst().getFailReason());
    }

    private static class InMemoryAlertRecordService extends AlertRecordServiceImpl {

        private final List<AlertRecord> savedItems = new ArrayList<>();
        private LocalDateTime fixedNow = LocalDateTime.now();
        private long nextId = 1L;

        @Override
        public AlertRecord getOne(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return savedItems.stream()
                    .filter(item -> "push_final_failed".equals(item.getAlertType()) || "task_failed".equals(item.getAlertType()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public boolean save(AlertRecord entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            savedItems.add(entity);
            return true;
        }

        @Override
        public boolean updateById(AlertRecord entity) {
            AlertRecord existing = savedItems.stream()
                    .filter(item -> item.getId().equals(entity.getId()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                return false;
            }
            if (entity.getAlertStatus() != null) {
                existing.setAlertStatus(entity.getAlertStatus());
            }
            if (entity.getAlertTitle() != null) {
                existing.setAlertTitle(entity.getAlertTitle());
            }
            if (entity.getAlertContent() != null) {
                existing.setAlertContent(entity.getAlertContent());
            }
            if (entity.getFailReason() != null || entity.getId() != null) {
                existing.setFailReason(entity.getFailReason());
            }
            if (entity.getLastOccurTime() != null) {
                existing.setLastOccurTime(entity.getLastOccurTime());
            }
            if (entity.getSendTime() != null || entity.getId() != null) {
                existing.setSendTime(entity.getSendTime());
            }
            return true;
        }

        @Override
        protected AlertRecord getExistingAlert(String alertType, String sourceType, Long sourceId) {
            return savedItems.stream()
                    .filter(item -> alertType.equals(item.getAlertType()))
                    .filter(item -> sourceType.equals(item.getSourceType()))
                    .filter(item -> (sourceId == null && item.getSourceId() == null)
                            || (sourceId != null && sourceId.equals(item.getSourceId())))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }
    }
}
