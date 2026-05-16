package com.pingyu.infodiet.service;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.service.impl.ContentSelectionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ContentSelectionServiceTest {

    @Test
    void listFeaturedContentItemsShouldUseScoreSortAndFeaturedThreshold() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        InfoDietProperties infoDietProperties = new InfoDietProperties();
        infoDietProperties.setFeaturedMinQualityScore(75);
        when(contentItemService.listUnifiedContentItems(Mockito.any())).thenReturn(List.of(
                UnifiedContentItemDTO.builder().id(1L).qualityScore(90).build()
        ));

        ContentSelectionServiceImpl service = new ContentSelectionServiceImpl();
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "infoDietProperties", infoDietProperties);

        List<UnifiedContentItemDTO> result = service.listFeaturedContentItems();

        assertEquals(1, result.size());
        ArgumentCaptor<UnifiedContentQueryRequest> captor = ArgumentCaptor.forClass(UnifiedContentQueryRequest.class);
        Mockito.verify(contentItemService).listUnifiedContentItems(captor.capture());
        assertEquals("score", captor.getValue().getSortBy());
        assertEquals(75, captor.getValue().getMinQualityScore());
    }
}
