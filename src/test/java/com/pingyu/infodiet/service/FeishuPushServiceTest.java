package com.pingyu.infodiet.service;

import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.FeishuPushServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FeishuPushServiceTest {

    @Test
    void buildFeishuRecordFieldsShouldMapContentItemFields() {
        TestableFeishuPushService service = new TestableFeishuPushService();

        ContentItem item = ContentItem.builder()
                .title("DeepSeek-TUI")
                .description("Coding agent for DeepSeek models")
                .contentUrl("https://github.com/Hmbown/DeepSeek-TUI")
                .authorName("Hmbown")
                .platform("github")
                .language("Rust")
                .starCount(9612)
                .todayStarCount(2434)
                .crawlDate(Date.valueOf(LocalDate.of(2026, 5, 7)))
                .build();

        Map<String, Object> fields = service.buildFields(item);

        assertEquals("DeepSeek-TUI", fields.get("标题"));
        assertEquals("Coding agent for DeepSeek models", fields.get("描述"));
        assertEquals("https://github.com/Hmbown/DeepSeek-TUI", fields.get("链接"));
        assertEquals("Hmbown", fields.get("作者"));
        assertEquals("github", fields.get("平台"));
        assertEquals("Rust", fields.get("语言"));
        assertEquals(9612, fields.get("总 Star 数"));
        assertEquals(2434, fields.get("今日新增 Star 数"));
        assertEquals("2026-05-07", fields.get("抓取日期"));
    }

    @Test
    void pushContentItemsToFeishuShouldMarkSuccessfulItems() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 7, 9, 30);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        TestableFeishuPushService service = new TestableFeishuPushService();
        service.fixedNow = fixedNow;
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        ContentItem firstItem = ContentItem.builder().id(1L).title("first").build();
        ContentItem secondItem = ContentItem.builder().id(2L).title("second").build();
        service.pendingItems.add(firstItem);
        service.pendingItems.add(secondItem);
        service.pushResults.add(true);
        service.pushResults.add(false);

        FeishuPushService.PushResult result = service.pushContentItemsToFeishu();

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        verify(contentItemService, times(1)).updateById(Mockito.argThat(item ->
                item.getId().equals(1L)
                        && item.getPushStatus() == 1
                        && fixedNow.equals(item.getPushTime())
        ));
    }

    @Test
    void pushContentItemsToFeishuShouldThrowWhenFeishuConfigIsMissing() {
        TestableFeishuPushService service = new TestableFeishuPushService();
        service.pendingItems.add(ContentItem.builder().id(1L).title("first").build());
        ReflectionTestUtils.setField(service, "feishuBaseProperties", new FeishuBaseProperties());

        assertThrows(BusinessException.class, service::pushContentItemsToFeishu);
    }

    private static class TestableFeishuPushService extends FeishuPushServiceImpl {

        private final List<ContentItem> pendingItems = new ArrayList<>();
        private final List<Boolean> pushResults = new ArrayList<>();
        private LocalDateTime fixedNow = LocalDateTime.now();

        private TestableFeishuPushService() {
            FeishuBaseProperties feishuBaseProperties = new FeishuBaseProperties();
            feishuBaseProperties.setAppId("cli_test");
            feishuBaseProperties.setAppSecret("secret_test");
            feishuBaseProperties.setAppToken("app_token_test");
            feishuBaseProperties.setTableId("table_id_test");
            ReflectionTestUtils.setField(this, "feishuBaseProperties", feishuBaseProperties);
        }

        @Override
        public List<ContentItem> listPendingPushItems() {
            return pendingItems;
        }

        @Override
        protected boolean pushSingleContentItem(ContentItem contentItem) {
            return pushResults.removeFirst();
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }

        private Map<String, Object> buildFields(ContentItem contentItem) {
            return buildFeishuRecordFields(contentItem);
        }
    }
}
