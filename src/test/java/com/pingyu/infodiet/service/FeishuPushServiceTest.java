package com.pingyu.infodiet.service;

import com.lark.oapi.Client;
import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestSectionDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
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
import static org.mockito.Mockito.when;

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
        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(true, null));
        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(false, "飞书接口失败"));

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

    @Test
    void pushUserContentItemsToFeishuShouldUpdateUserPushStatus() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 7, 9, 30);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);

        TestableFeishuPushService service = new TestableFeishuPushService();
        service.fixedNow = fixedNow;
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        UserContentPush firstPush = UserContentPush.builder().id(11L).contentItemId(1L).build();
        UserContentPush secondPush = UserContentPush.builder().id(12L).contentItemId(2L).build();
        service.pendingUserPushItems.add(firstPush);
        service.pendingUserPushItems.add(secondPush);

        when(contentItemService.getById(1L)).thenReturn(ContentItem.builder().id(1L).title("first").build());
        when(contentItemService.getById(2L)).thenReturn(ContentItem.builder().id(2L).title("second").build());

        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(true, null));
        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(false, "code=91403,msg=Forbidden"));

        FeishuPushService.PushResult result = service.pushUserContentItemsToFeishu();

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        verify(userContentPushService, times(1)).markPushSuccess(11L);
        verify(userContentPushService, times(1)).markPushFailed(12L, "code=91403,msg=Forbidden");
    }

    @Test
    void pushSingleUserContentItemToFeishuShouldUpdatePushStatus() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);

        TestableFeishuPushService service = new TestableFeishuPushService();
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        when(userContentPushService.getById(11L)).thenReturn(
                UserContentPush.builder().id(11L).contentItemId(1L).build()
        );
        when(contentItemService.getById(1L)).thenReturn(ContentItem.builder().id(1L).title("first").build());
        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(true, null));

        boolean result = service.pushSingleUserContentItemToFeishu(11L);

        assertEquals(true, result);
        verify(userContentPushService, times(1)).markPushSuccess(11L);
    }

    @Test
    void pushSingleUserContentItemToFeishuShouldCreateAlertWhenFinalFailed() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);

        TestableFeishuPushService service = new TestableFeishuPushService();
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        when(userContentPushService.getById(11L)).thenReturn(
                UserContentPush.builder().id(11L).contentItemId(1L).retryCount(2).maxRetryCount(3).build()
        );
        when(contentItemService.getById(1L)).thenReturn(ContentItem.builder().id(1L).title("first").build());
        service.pushResults.add(new FeishuPushServiceImpl.PushAttemptResult(false, "code=91403,msg=Forbidden"));

        boolean result = service.pushSingleUserContentItemToFeishu(11L);

        assertEquals(false, result);
        verify(userContentPushService, times(1)).markPushFailed(11L, "code=91403,msg=Forbidden");
        verify(alertRecordService, times(1)).createOrUpdateAlert(
                Mockito.eq("push_final_failed"),
                Mockito.eq("error"),
                Mockito.eq("user_content_push"),
                Mockito.eq(11L),
                Mockito.eq("用户内容推送最终失败"),
                Mockito.contains("code=91403,msg=Forbidden")
        );
    }

    @Test
    void pushTodayDigestToFeishuShouldSendDigestMessageToEnabledFeishuUsers() {
        DailyDigestService dailyDigestService = Mockito.mock(DailyDigestService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        when(dailyDigestService.generateTodayDigest()).thenReturn(DailyDigestDTO.builder()
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(2)
                .totalItemCount(3)
                .summary("今日共筛出 2 条精选事件。")
                .sections(List.of(
                        DailyDigestSectionDTO.builder()
                                .sectionTitle("仓库 / 项目")
                                .clusters(List.of(
                                        ContentEventClusterDTO.builder()
                                                .clusterTitle("OpenAI releases GPT-5.5")
                                                .clusterScore(92)
                                                .clusterSize(2)
                                                .build()
                                ))
                                .build()
                ))
                .build());
        when(userProfileService.listEnabledUsers()).thenReturn(List.of(
                UserProfile.builder().id(1L).pushChannel("feishu").feishuUserId("ou_1").status(1).build(),
                UserProfile.builder().id(2L).pushChannel("telegram").feishuUserId("ou_2").status(1).build(),
                UserProfile.builder().id(3L).pushChannel("feishu").feishuUserId("ou_3").status(1).build()
        ));

        TestableFeishuPushService service = new TestableFeishuPushService();
        ReflectionTestUtils.setField(service, "dailyDigestService", dailyDigestService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        service.messageResults.add(true);
        service.messageResults.add(false);

        FeishuPushService.PushResult result = service.pushTodayDigestToFeishu();

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(2, service.sentMessages.size());
        assertEquals("ou_1", service.sentMessages.get(0).receiveId());
        assertEquals("ou_3", service.sentMessages.get(1).receiveId());
    }

    private static class TestableFeishuPushService extends FeishuPushServiceImpl {

        private final List<ContentItem> pendingItems = new ArrayList<>();
        private final List<UserContentPush> pendingUserPushItems = new ArrayList<>();
        private final List<FeishuPushServiceImpl.PushAttemptResult> pushResults = new ArrayList<>();
        private final List<Boolean> messageResults = new ArrayList<>();
        private final List<SentMessage> sentMessages = new ArrayList<>();
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
        protected List<UserContentPush> listPendingUserPushItems() {
            return pendingUserPushItems;
        }

        @Override
        protected FeishuPushServiceImpl.PushAttemptResult pushSingleContentItemWithResult(ContentItem contentItem) {
            return pushResults.removeFirst();
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }

        @Override
        protected boolean sendTextMessage(Client client, String receiveId, String content) {
            sentMessages.add(new SentMessage(receiveId, content));
            return messageResults.removeFirst();
        }

        private Map<String, Object> buildFields(ContentItem contentItem) {
            return buildFeishuRecordFields(contentItem);
        }

        private record SentMessage(String receiveId, String content) {
        }
    }
}
