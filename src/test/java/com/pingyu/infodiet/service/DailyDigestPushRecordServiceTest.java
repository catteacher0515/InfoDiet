package com.pingyu.infodiet.service;

import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestPushRecord;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.DailyDigestPushRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class DailyDigestPushRecordServiceTest {

    @Test
    void saveOrUpdatePushRecordShouldInsertWhenRecordNotExists() {
        DailyDigestPushRecordServiceImpl service = Mockito.spy(new DailyDigestPushRecordServiceImpl());
        Mockito.doReturn(null).when(service).getByDigestDateAndUserIdAndPushChannel(LocalDate.of(2026, 5, 16), 1L, "feishu");
        Mockito.doReturn(true).when(service).save(any(DailyDigestPushRecord.class));

        DailyDigestDTO digest = DailyDigestDTO.builder()
                .digestDate(LocalDate.of(2026, 5, 16))
                .digestTitle("AI 日报 · 2026-05-16")
                .build();
        UserProfile userProfile = UserProfile.builder()
                .id(1L)
                .pushChannel("feishu")
                .feishuUserId("ou_1")
                .build();

        DailyDigestPushRecord result = service.saveOrUpdatePushRecord(
                digest,
                userProfile,
                "feishu",
                true,
                null,
                LocalDateTime.of(2026, 5, 16, 8, 0)
        );

        ArgumentCaptor<DailyDigestPushRecord> captor = ArgumentCaptor.forClass(DailyDigestPushRecord.class);
        verify(service).save(captor.capture());
        assertEquals(LocalDate.of(2026, 5, 16), captor.getValue().getDigestDate());
        assertEquals(1, captor.getValue().getPushStatus());
        assertNotNull(result);
    }

    @Test
    void saveOrUpdatePushRecordShouldUpdateWhenRecordExists() {
        DailyDigestPushRecordServiceImpl service = Mockito.spy(new DailyDigestPushRecordServiceImpl());
        Mockito.doReturn(DailyDigestPushRecord.builder().id(10L).digestDate(LocalDate.of(2026, 5, 16)).build())
                .when(service).getByDigestDateAndUserIdAndPushChannel(LocalDate.of(2026, 5, 16), 1L, "feishu");
        Mockito.doReturn(true).when(service).updateById(any(DailyDigestPushRecord.class));

        DailyDigestPushRecord result = service.saveOrUpdatePushRecord(
                DailyDigestDTO.builder().digestDate(LocalDate.of(2026, 5, 16)).digestTitle("AI 日报 · 2026-05-16").build(),
                UserProfile.builder().id(1L).pushChannel("feishu").feishuUserId("ou_1").build(),
                "feishu",
                false,
                "forbidden",
                LocalDateTime.of(2026, 5, 16, 8, 0)
        );

        ArgumentCaptor<DailyDigestPushRecord> captor = ArgumentCaptor.forClass(DailyDigestPushRecord.class);
        verify(service).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(2, captor.getValue().getPushStatus());
        assertEquals("forbidden", captor.getValue().getFailReason());
        assertNotNull(result);
    }

    @Test
    void pagePushRecordsShouldFilterByStatusAndKeyword() {
        InMemoryDailyDigestPushRecordService service = new InMemoryDailyDigestPushRecordService();
        service.records.add(DailyDigestPushRecord.builder()
                .id(1L).digestDate(LocalDate.of(2026, 5, 16)).digestTitle("AI 日报 A")
                .userId(1L).pushChannel("feishu").receiveId("ou_1").pushStatus(1).build());
        service.records.add(DailyDigestPushRecord.builder()
                .id(2L).digestDate(LocalDate.of(2026, 5, 16)).digestTitle("AI 日报 B")
                .userId(2L).pushChannel("feishu").receiveId("ou_2").pushStatus(2).failReason("forbidden").build());
        service.records.add(DailyDigestPushRecord.builder()
                .id(3L).digestDate(LocalDate.of(2026, 5, 15)).digestTitle("AI 日报 C")
                .userId(3L).pushChannel("telegram").receiveId("tg_3").pushStatus(2).failReason("token expired").build());

        PageResponse<DailyDigestPushRecord> response = service.pagePushRecords(2, "forbidden", null, 1, 10);

        assertEquals(1, response.getTotalCount());
        assertEquals(2L, response.getRecords().getFirst().getId());
    }

    @Test
    void pagePushRecordsShouldFilterByDigestDate() {
        InMemoryDailyDigestPushRecordService service = new InMemoryDailyDigestPushRecordService();
        service.records.add(DailyDigestPushRecord.builder()
                .id(1L).digestDate(LocalDate.of(2026, 5, 16)).digestTitle("AI 日报 A")
                .userId(1L).pushChannel("feishu").receiveId("ou_1").pushStatus(1).build());
        service.records.add(DailyDigestPushRecord.builder()
                .id(2L).digestDate(LocalDate.of(2026, 5, 15)).digestTitle("AI 日报 B")
                .userId(2L).pushChannel("feishu").receiveId("ou_2").pushStatus(2).failReason("forbidden").build());

        PageResponse<DailyDigestPushRecord> response = service.pagePushRecords(null, null, LocalDate.of(2026, 5, 16), 1, 10);

        assertEquals(1, response.getTotalCount());
        assertEquals(1L, response.getRecords().getFirst().getId());
    }

    @Test
    void listRecentFailedRecordsShouldOnlyReturnFailedRecords() {
        InMemoryDailyDigestPushRecordService service = new InMemoryDailyDigestPushRecordService();
        service.records.add(DailyDigestPushRecord.builder().id(1L).pushStatus(1).pushChannel("feishu").build());
        service.records.add(DailyDigestPushRecord.builder().id(2L).pushStatus(2).pushChannel("feishu").build());
        service.records.add(DailyDigestPushRecord.builder().id(3L).pushStatus(2).pushChannel("feishu").build());

        List<DailyDigestPushRecord> result = service.listRecentFailedRecords(10);

        assertEquals(2, result.size());
        assertEquals(3L, result.getFirst().getId());
    }

    @Test
    void saveOrUpdatePushRecordShouldFallbackToUpdateWhenConcurrentInsertCausesDuplicateKey() {
        DailyDigestPushRecordServiceImpl service = Mockito.spy(new DailyDigestPushRecordServiceImpl());
        LocalDate digestDate = LocalDate.of(2026, 5, 16);
        DailyDigestPushRecord existing = DailyDigestPushRecord.builder().id(18L).digestDate(digestDate).build();
        Mockito.doReturn(null, existing)
                .when(service).getByDigestDateAndUserIdAndPushChannel(digestDate, 1L, "feishu");
        Mockito.doThrow(new DuplicateKeyException("duplicate digest user channel"))
                .when(service).save(any(DailyDigestPushRecord.class));
        Mockito.doReturn(true).when(service).updateById(any(DailyDigestPushRecord.class));

        DailyDigestPushRecord result = service.saveOrUpdatePushRecord(
                DailyDigestDTO.builder().digestDate(digestDate).digestTitle("AI 日报 · 2026-05-16").build(),
                UserProfile.builder().id(1L).pushChannel("feishu").feishuUserId("ou_1").build(),
                "feishu",
                false,
                "network timeout",
                LocalDateTime.of(2026, 5, 16, 9, 0)
        );

        ArgumentCaptor<DailyDigestPushRecord> captor = ArgumentCaptor.forClass(DailyDigestPushRecord.class);
        verify(service).updateById(captor.capture());
        assertEquals(18L, captor.getValue().getId());
        assertEquals(2, captor.getValue().getPushStatus());
        assertEquals("network timeout", captor.getValue().getFailReason());
        assertNotNull(result);
        assertEquals(18L, result.getId());
    }

    private static class InMemoryDailyDigestPushRecordService extends DailyDigestPushRecordServiceImpl {

        private final java.util.List<DailyDigestPushRecord> records = new java.util.ArrayList<>();

        @Override
        public PageResponse<DailyDigestPushRecord> pagePushRecords(Integer pushStatus, String keyword, LocalDate digestDate, int pageNum, int pageSize) {
            java.util.List<DailyDigestPushRecord> filtered = records.stream()
                    .filter(item -> pushStatus == null || pushStatus.equals(item.getPushStatus()))
                    .filter(item -> digestDate == null || digestDate.equals(item.getDigestDate()))
                    .filter(item -> keyword == null
                            || String.valueOf(item.getId()).contains(keyword)
                            || String.valueOf(item.getUserId()).contains(keyword)
                            || (item.getReceiveId() != null && item.getReceiveId().contains(keyword))
                            || (item.getDigestTitle() != null && item.getDigestTitle().contains(keyword))
                            || (item.getFailReason() != null && item.getFailReason().contains(keyword)))
                    .sorted(java.util.Comparator.comparing(DailyDigestPushRecord::getId).reversed())
                    .toList();
            int safePageNum = Math.max(pageNum, 1);
            int safePageSize = Math.max(pageSize, 1);
            int fromIndex = Math.min((safePageNum - 1) * safePageSize, filtered.size());
            int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
            return new PageResponse<>(filtered.size(), safePageNum, safePageSize, filtered.subList(fromIndex, toIndex));
        }

        @Override
        public java.util.List<DailyDigestPushRecord> listRecentFailedRecords(int limit) {
            return records.stream()
                    .filter(item -> item.getPushStatus() != null && item.getPushStatus() == 2)
                    .sorted(java.util.Comparator.comparing(DailyDigestPushRecord::getId).reversed())
                    .limit(limit)
                    .toList();
        }
    }
}
