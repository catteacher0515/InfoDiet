package com.pingyu.infodiet.service;

import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.impl.UserContentPushServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserContentPushServiceQueryTest {

    @Test
    void pageFailedPushesShouldFilterByKeywordAndRetryCount() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.items.add(UserContentPush.builder().id(1L).userId(1L).contentItemId(10L).pushChannel("feishu").pushStatus(2).retryCount(1).failReason("token expired").build());
        service.items.add(UserContentPush.builder().id(2L).userId(2L).contentItemId(11L).pushChannel("feishu").pushStatus(2).retryCount(3).failReason("field missing").build());
        service.items.add(UserContentPush.builder().id(3L).userId(3L).contentItemId(12L).pushChannel("telegram").pushStatus(2).retryCount(2).failReason("token expired").build());

        PageResponse<UserContentPush> response = service.pageFailedPushes("feishu", "field", 3, 1, 10);

        assertEquals(1, response.getRecords().size());
        assertEquals(2L, response.getRecords().getFirst().getId());
        assertEquals(1, response.getTotalCount());
    }

    @Test
    void pageFailedPushesShouldRespectPageSize() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.items.add(UserContentPush.builder().id(1L).userId(1L).contentItemId(10L).pushChannel("feishu").pushStatus(2).build());
        service.items.add(UserContentPush.builder().id(2L).userId(2L).contentItemId(11L).pushChannel("feishu").pushStatus(2).build());
        service.items.add(UserContentPush.builder().id(3L).userId(3L).contentItemId(12L).pushChannel("feishu").pushStatus(2).build());

        PageResponse<UserContentPush> response = service.pageFailedPushes("feishu", null, null, 2, 2);

        assertEquals(1, response.getRecords().size());
        assertEquals(1L, response.getRecords().getFirst().getId());
        assertEquals(3, response.getTotalCount());
    }

    private static class InMemoryUserContentPushService extends UserContentPushServiceImpl {

        private final List<UserContentPush> items = new ArrayList<>();

        @Override
        public List<UserContentPush> list() {
            return items;
        }

        @Override
        public PageResponse<UserContentPush> pageFailedPushes(String pushChannel, String keyword, Integer retryCount, int pageNum, int pageSize) {
            List<UserContentPush> filtered = items.stream()
                    .filter(item -> item.getPushStatus() != null && item.getPushStatus() == 2)
                    .filter(item -> pushChannel == null || pushChannel.equals(item.getPushChannel()))
                    .filter(item -> retryCount == null || retryCount.equals(item.getRetryCount()))
                    .filter(item -> keyword == null
                            || String.valueOf(item.getId()).contains(keyword)
                            || String.valueOf(item.getUserId()).contains(keyword)
                            || String.valueOf(item.getContentItemId()).contains(keyword)
                            || (item.getFailReason() != null && item.getFailReason().contains(keyword)))
                    .sorted(Comparator.comparing(UserContentPush::getId).reversed())
                    .toList();
            int safePageNum = Math.max(pageNum, 1);
            int safePageSize = Math.max(pageSize, 1);
            int fromIndex = Math.min((safePageNum - 1) * safePageSize, filtered.size());
            int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
            return new PageResponse<>(filtered.size(), safePageNum, safePageSize, filtered.subList(fromIndex, toIndex));
        }
    }
}
