package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.entity.DailyDigestPushRecord;
import com.pingyu.infodiet.service.DailyDigestPushRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DailyDigestPushRecordControllerTest {

    @Test
    void listRecentFailedRecordsShouldReturnRecords() {
        DailyDigestPushRecordService service = Mockito.mock(DailyDigestPushRecordService.class);
        when(service.listRecentFailedRecords(10)).thenReturn(List.of(
                DailyDigestPushRecord.builder().id(2L).pushStatus(2).failReason("forbidden").build()
        ));

        DailyDigestPushRecordController controller = new DailyDigestPushRecordController();
        ReflectionTestUtils.setField(controller, "dailyDigestPushRecordService", service);

        BaseResponse<List<DailyDigestPushRecord>> response = controller.listRecentFailedRecords(10);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(2L, response.getData().getFirst().getId());
    }

    @Test
    void pagePushRecordsShouldReturnPageResponse() {
        DailyDigestPushRecordService service = Mockito.mock(DailyDigestPushRecordService.class);
        when(service.pagePushRecords(2, "forbidden", java.time.LocalDate.of(2026, 5, 16), 1, 10)).thenReturn(
                new PageResponse<>(1, 1, 10, List.of(
                        DailyDigestPushRecord.builder().id(2L).pushStatus(2).failReason("forbidden").build()
                ))
        );

        DailyDigestPushRecordController controller = new DailyDigestPushRecordController();
        ReflectionTestUtils.setField(controller, "dailyDigestPushRecordService", service);

        BaseResponse<PageResponse<DailyDigestPushRecord>> response = controller.pagePushRecords(2, "forbidden", "2026-05-16", 1, 10);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().getTotalCount());
        assertEquals(2L, response.getData().getRecords().getFirst().getId());
    }
}
