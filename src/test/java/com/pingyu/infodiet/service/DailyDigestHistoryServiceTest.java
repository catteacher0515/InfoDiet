package com.pingyu.infodiet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestHistory;
import com.pingyu.infodiet.service.impl.DailyDigestHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyDigestHistoryServiceTest {

    @Test
    void saveOrUpdateDigestShouldInsertNewHistoryWhenDateNotExists() {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        Mockito.doReturn(null).when(service).getByDigestDate(LocalDate.of(2026, 5, 16));
        Mockito.doReturn(true).when(service).save(any(DailyDigestHistory.class));

        DailyDigestDTO digest = DailyDigestDTO.builder()
                .digestDate(LocalDate.of(2026, 5, 16))
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(2)
                .totalItemCount(3)
                .summary("今日共筛出 2 条精选事件。")
                .build();

        service.saveOrUpdateDigest(digest);

        ArgumentCaptor<DailyDigestHistory> captor = ArgumentCaptor.forClass(DailyDigestHistory.class);
        verify(service).save(captor.capture());
        assertEquals(LocalDate.of(2026, 5, 16), captor.getValue().getDigestDate());
        assertEquals(2, captor.getValue().getTotalClusterCount());
    }

    @Test
    void saveOrUpdateDigestShouldUpdateExistingHistoryWhenDateExists() {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        Mockito.doReturn(DailyDigestHistory.builder().id(1L).digestDate(LocalDate.of(2026, 5, 16)).build())
                .when(service).getByDigestDate(LocalDate.of(2026, 5, 16));
        Mockito.doReturn(true).when(service).updateById(any(DailyDigestHistory.class));

        DailyDigestDTO digest = DailyDigestDTO.builder()
                .digestDate(LocalDate.of(2026, 5, 16))
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(3)
                .totalItemCount(5)
                .summary("更新后的日报")
                .build();

        service.saveOrUpdateDigest(digest);

        ArgumentCaptor<DailyDigestHistory> captor = ArgumentCaptor.forClass(DailyDigestHistory.class);
        verify(service).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(3, captor.getValue().getTotalClusterCount());
    }

    @Test
    void listRecentDigestShouldConvertHistoryToDtoList() throws JsonProcessingException {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        DailyDigestDTO digest = DailyDigestDTO.builder()
                .digestDate(LocalDate.of(2026, 5, 16))
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(2)
                .totalItemCount(3)
                .summary("今日共筛出 2 条精选事件。")
                .build();
        Mockito.doReturn(List.of(
                DailyDigestHistory.builder()
                        .id(1L)
                        .digestDate(LocalDate.of(2026, 5, 16))
                        .digestTitle("AI 日报 · 2026-05-16")
                        .totalClusterCount(2)
                        .totalItemCount(3)
                        .summary("今日共筛出 2 条精选事件。")
                        .digestContent(objectMapper.writeValueAsString(digest))
                        .build()
        )).when(service).list(any(QueryWrapper.class));

        List<DailyDigestDTO> result = service.listRecentDigests(7);

        assertEquals(1, result.size());
        assertEquals("AI 日报 · 2026-05-16", result.getFirst().getDigestTitle());
    }

    @Test
    void getDigestByDateShouldReturnNullWhenHistoryNotExists() {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().findAndRegisterModules());
        Mockito.doReturn(null).when(service).getOne(any(QueryWrapper.class));

        DailyDigestDTO result = service.getDigestDTOByDate(LocalDate.of(2026, 5, 16));

        assertNull(result);
    }

    @Test
    void saveOrUpdateDigestShouldThrowWhenSerializeFailed() throws JsonProcessingException {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        Mockito.doReturn(null).when(service).getByDigestDate(LocalDate.of(2026, 5, 16));
        when(objectMapper.copy()).thenReturn(objectMapper);
        when(objectMapper.findAndRegisterModules()).thenReturn(objectMapper);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialize failed") {});

        DailyDigestDTO digest = DailyDigestDTO.builder()
                .digestDate(LocalDate.of(2026, 5, 16))
                .digestTitle("AI 日报 · 2026-05-16")
                .build();

        assertThrows(IllegalStateException.class, () -> service.saveOrUpdateDigest(digest));
    }

    @Test
    void saveOrUpdateDigestShouldFallbackToUpdateWhenConcurrentInsertCausesDuplicateKey() {
        DailyDigestHistoryServiceImpl service = Mockito.spy(new DailyDigestHistoryServiceImpl());
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        LocalDate digestDate = LocalDate.of(2026, 5, 16);
        DailyDigestHistory existing = DailyDigestHistory.builder().id(9L).digestDate(digestDate).build();
        Mockito.doReturn(null, existing).when(service).getByDigestDate(digestDate);
        Mockito.doThrow(new DuplicateKeyException("duplicate digest date"))
                .when(service).save(any(DailyDigestHistory.class));
        Mockito.doReturn(true).when(service).updateById(any(DailyDigestHistory.class));

        DailyDigestDTO result = DailyDigestDTO.builder()
                .digestDate(digestDate)
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(5)
                .totalItemCount(8)
                .summary("并发重试后的日报")
                .build();

        DailyDigestHistory history = service.saveOrUpdateDigest(result);

        ArgumentCaptor<DailyDigestHistory> captor = ArgumentCaptor.forClass(DailyDigestHistory.class);
        verify(service).updateById(captor.capture());
        assertEquals(9L, captor.getValue().getId());
        assertEquals(5, captor.getValue().getTotalClusterCount());
        assertEquals(9L, history.getId());
    }
}
