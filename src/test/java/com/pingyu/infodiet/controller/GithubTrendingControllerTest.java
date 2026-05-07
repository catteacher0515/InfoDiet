package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.GithubTrendingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class GithubTrendingControllerTest {

    @Test
    void crawlAndSaveGitHubTrendingShouldReturnSaveSummary() {
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        List<GithubTrendingItemDTO> dtoList = List.of(new GithubTrendingItemDTO(), new GithubTrendingItemDTO());
        ContentItemService.SaveResult saveResult = new ContentItemService.SaveResult(2, 1, 1);

        when(githubTrendingService.crawlGitHubTrending()).thenReturn(dtoList);
        when(contentItemService.saveGithubTrendingItems(dtoList)).thenReturn(saveResult);

        GithubTrendingController controller = new GithubTrendingController();
        ReflectionTestUtils.setField(controller, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(controller, "contentItemService", contentItemService);

        BaseResponse<ContentItemService.SaveResult> response = controller.crawlAndSaveGitHubTrending();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getTotalCount());
        assertEquals(1, response.getData().getSavedCount());
        assertEquals(1, response.getData().getSkippedCount());
    }
}
