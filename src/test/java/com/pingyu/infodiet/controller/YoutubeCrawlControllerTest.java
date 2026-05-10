package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.YoutubeCrawlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class YoutubeCrawlControllerTest {

    @Test
    void crawlYoutubeVideosShouldReturnVideoList() {
        YoutubeCrawlService youtubeCrawlService = Mockito.mock(YoutubeCrawlService.class);
        List<YoutubeVideoItemDTO> dtoList = List.of(new YoutubeVideoItemDTO(), new YoutubeVideoItemDTO());

        when(youtubeCrawlService.crawlYoutubeVideos("UC123456")).thenReturn(dtoList);

        YoutubeCrawlController controller = new YoutubeCrawlController();
        ReflectionTestUtils.setField(controller, "youtubeCrawlService", youtubeCrawlService);

        BaseResponse<List<YoutubeVideoItemDTO>> response = controller.crawlYoutubeVideos("UC123456");

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().size());
    }

    @Test
    void crawlAndSaveYoutubeVideosShouldReturnSaveSummary() {
        YoutubeCrawlService youtubeCrawlService = Mockito.mock(YoutubeCrawlService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        List<YoutubeVideoItemDTO> dtoList = List.of(new YoutubeVideoItemDTO(), new YoutubeVideoItemDTO());
        ContentItemService.SaveResult saveResult = new ContentItemService.SaveResult(2, 1, 1);

        when(youtubeCrawlService.crawlYoutubeVideos("UC123456")).thenReturn(dtoList);
        when(contentItemService.saveYoutubeVideoItems(dtoList)).thenReturn(saveResult);

        YoutubeCrawlController controller = new YoutubeCrawlController();
        ReflectionTestUtils.setField(controller, "youtubeCrawlService", youtubeCrawlService);
        ReflectionTestUtils.setField(controller, "contentItemService", contentItemService);

        BaseResponse<ContentItemService.SaveResult> response = controller.crawlAndSaveYoutubeVideos("UC123456");

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getTotalCount());
        assertEquals(1, response.getData().getSavedCount());
        assertEquals(1, response.getData().getSkippedCount());
    }
}
