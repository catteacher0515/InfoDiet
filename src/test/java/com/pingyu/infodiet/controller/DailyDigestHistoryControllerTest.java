package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.service.DailyDigestHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DailyDigestHistoryControllerTest {

    @Test
    void listRecentDigestsShouldReturnDigestList() {
        DailyDigestHistoryService dailyDigestHistoryService = Mockito.mock(DailyDigestHistoryService.class);
        when(dailyDigestHistoryService.listRecentDigests(7)).thenReturn(List.of(
                DailyDigestDTO.builder()
                        .digestDate(LocalDate.of(2026, 5, 16))
                        .digestTitle("AI 日报 · 2026-05-16")
                        .build()
        ));

        DailyDigestHistoryController controller = new DailyDigestHistoryController();
        ReflectionTestUtils.setField(controller, "dailyDigestHistoryService", dailyDigestHistoryService);

        BaseResponse<List<DailyDigestDTO>> response = controller.listRecentDigests(7);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("AI 日报 · 2026-05-16", response.getData().getFirst().getDigestTitle());
    }

    @Test
    void getDigestByDateShouldReturnDigestDetail() {
        DailyDigestHistoryService dailyDigestHistoryService = Mockito.mock(DailyDigestHistoryService.class);
        when(dailyDigestHistoryService.getDigestDTOByDate(LocalDate.of(2026, 5, 16))).thenReturn(
                DailyDigestDTO.builder()
                        .digestDate(LocalDate.of(2026, 5, 16))
                        .digestTitle("AI 日报 · 2026-05-16")
                        .totalClusterCount(2)
                        .build()
        );

        DailyDigestHistoryController controller = new DailyDigestHistoryController();
        ReflectionTestUtils.setField(controller, "dailyDigestHistoryService", dailyDigestHistoryService);

        BaseResponse<DailyDigestDTO> response = controller.getDigestByDate(LocalDate.of(2026, 5, 16).toString());

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getTotalClusterCount());
    }
}
