package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SubscriptionMatchControllerTest {

    @Test
    void matchEnabledUsersShouldReturnMatchedContentMap() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);

        Map<Long, List<ContentItem>> matchResult = Map.of(
                1L, List.of(
                        ContentItem.builder().id(103L).title("Gemini API update").build(),
                        ContentItem.builder().id(101L).title("agent workflow").build()
                ),
                2L, List.of(ContentItem.builder().id(102L).title("java tutorial").build())
        );
        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(matchResult);

        SubscriptionMatchController controller = new SubscriptionMatchController();
        ReflectionTestUtils.setField(controller, "subscriptionMatchService", subscriptionMatchService);

        BaseResponse<Map<Long, List<ContentItem>>> response = controller.matchEnabledUsers();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().size());
        assertEquals(103L, response.getData().get(1L).getFirst().getId());
        assertEquals(102L, response.getData().get(2L).getFirst().getId());
    }

    @Test
    void matchEnabledUsersWithDetailsShouldReturnMatchDetails() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);

        Map<Long, List<SubscriptionMatchService.MatchDetail>> matchResult = Map.of(
                1L, List.of(new SubscriptionMatchService.MatchDetail(
                        ContentItem.builder().id(103L).title("Gemini API update").build(),
                        8,
                        List.of("author:Google for Developers", "keyword_include:agent")
                ))
        );
        when(subscriptionMatchService.matchEnabledUsersWithDetails()).thenReturn(matchResult);

        SubscriptionMatchController controller = new SubscriptionMatchController();
        ReflectionTestUtils.setField(controller, "subscriptionMatchService", subscriptionMatchService);

        BaseResponse<Map<Long, List<SubscriptionMatchService.MatchDetail>>> response =
                controller.matchEnabledUsersWithDetails();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(8, response.getData().get(1L).getFirst().getScore());
    }
}
