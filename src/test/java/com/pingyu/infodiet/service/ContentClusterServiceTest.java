package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.service.impl.ContentClusterServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ContentClusterServiceTest {

    @Test
    void clusterContentItemsShouldGroupSimilarFeaturedItems() {
        ContentClusterServiceImpl service = new ContentClusterServiceImpl();

        List<ContentEventClusterDTO> result = service.clusterContentItems(List.of(
                UnifiedContentItemDTO.builder()
                        .id(1L)
                        .title("OpenAI releases GPT-5.5 model")
                        .qualityScore(92)
                        .sortTime(LocalDateTime.of(2026, 5, 16, 10, 0))
                        .build(),
                UnifiedContentItemDTO.builder()
                        .id(2L)
                        .title("GPT-5.5 model released by OpenAI")
                        .qualityScore(86)
                        .sortTime(LocalDateTime.of(2026, 5, 16, 9, 0))
                        .build(),
                UnifiedContentItemDTO.builder()
                        .id(3L)
                        .title("Spring AI tutorial for Java developers")
                        .qualityScore(80)
                        .sortTime(LocalDateTime.of(2026, 5, 16, 8, 0))
                        .build()
        ));

        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().getPrimaryItem().getId());
        assertEquals(2, result.getFirst().getClusterSize());
        assertEquals(List.of(1L, 2L), result.getFirst().getRelatedItems().stream().map(UnifiedContentItemDTO::getId).toList());
    }

    @Test
    void listFeaturedClustersShouldClusterFeaturedItemsFromSelectionService() {
        ContentSelectionService contentSelectionService = Mockito.mock(ContentSelectionService.class);
        when(contentSelectionService.listFeaturedContentItems()).thenReturn(List.of(
                UnifiedContentItemDTO.builder()
                        .id(10L)
                        .title("OpenAI launches new agent API")
                        .qualityScore(90)
                        .sortTime(LocalDateTime.of(2026, 5, 16, 11, 0))
                        .build(),
                UnifiedContentItemDTO.builder()
                        .id(11L)
                        .title("New agent API launched by OpenAI")
                        .qualityScore(84)
                        .sortTime(LocalDateTime.of(2026, 5, 16, 10, 0))
                        .build()
        ));

        ContentClusterServiceImpl service = new ContentClusterServiceImpl();
        ReflectionTestUtils.setField(service, "contentSelectionService", contentSelectionService);

        List<ContentEventClusterDTO> result = service.listFeaturedClusters();

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getPrimaryItem().getId());
        assertEquals(2, result.getFirst().getClusterSize());
    }
}
