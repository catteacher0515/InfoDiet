package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.service.DailyDigestHistoryService;
import com.pingyu.infodiet.service.impl.DailyDigestServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyDigestServiceTest {

    @Test
    void generateTodayDigestShouldBuildGroupedDigestFromFeaturedClusters() {
        ContentClusterService contentClusterService = Mockito.mock(ContentClusterService.class);
        DailyDigestHistoryService dailyDigestHistoryService = Mockito.mock(DailyDigestHistoryService.class);
        when(contentClusterService.listFeaturedClusters()).thenReturn(List.of(
                ContentEventClusterDTO.builder()
                        .clusterKey("cluster-1")
                        .clusterTitle("OpenAI releases GPT-5.5")
                        .clusterScore(92)
                        .clusterSize(2)
                        .primaryItem(UnifiedContentItemDTO.builder()
                                .id(1L)
                                .platform("github")
                                .contentType("repository")
                                .title("OpenAI releases GPT-5.5")
                                .qualityScore(92)
                                .sortTime(LocalDateTime.of(2026, 5, 16, 10, 0))
                                .build())
                        .relatedItems(List.of(
                                UnifiedContentItemDTO.builder().id(1L).platform("github").build(),
                                UnifiedContentItemDTO.builder().id(2L).platform("youtube").build()
                        ))
                        .build(),
                ContentEventClusterDTO.builder()
                        .clusterKey("cluster-2")
                        .clusterTitle("Spring AI Java tutorial")
                        .clusterScore(80)
                        .clusterSize(1)
                        .primaryItem(UnifiedContentItemDTO.builder()
                                .id(3L)
                                .platform("youtube")
                                .contentType("video")
                                .title("Spring AI Java tutorial")
                                .qualityScore(80)
                                .sortTime(LocalDateTime.of(2026, 5, 16, 8, 0))
                                .build())
                        .relatedItems(List.of(
                                UnifiedContentItemDTO.builder().id(3L).platform("youtube").build()
                        ))
                        .build()
        ));

        DailyDigestServiceImpl service = new DailyDigestServiceImpl();
        ReflectionTestUtils.setField(service, "contentClusterService", contentClusterService);
        ReflectionTestUtils.setField(service, "dailyDigestHistoryService", dailyDigestHistoryService);
        ReflectionTestUtils.setField(service, "fixedToday", LocalDate.of(2026, 5, 16));

        DailyDigestDTO digest = service.generateTodayDigest();

        assertEquals(LocalDate.of(2026, 5, 16), digest.getDigestDate());
        assertEquals("AI 日报 · 2026-05-16", digest.getDigestTitle());
        assertEquals(2, digest.getTotalClusterCount());
        assertEquals(3, digest.getTotalItemCount());
        assertEquals(2, digest.getSections().size());
        assertEquals("仓库 / 项目", digest.getSections().get(0).getSectionTitle());
        assertEquals("视频 / 讲解", digest.getSections().get(1).getSectionTitle());
        assertTrue(digest.getSummary().contains("OpenAI releases GPT-5.5"));
        verify(dailyDigestHistoryService).saveOrUpdateDigest(any(DailyDigestDTO.class));
    }
}
