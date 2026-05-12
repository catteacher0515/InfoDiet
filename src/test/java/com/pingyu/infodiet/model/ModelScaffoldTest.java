package com.pingyu.infodiet.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.pingyu.infodiet.generator.MyBatisCodeGenerator;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelScaffoldTest {

    @Test
    void contentItemShouldBeMyBatisFlexEntityForContentItemTable() throws NoSuchFieldException {
        Table table = ContentItem.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("content_item", table.value());
        assertTrue(Serializable.class.isAssignableFrom(ContentItem.class));

        Field idField = ContentItem.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(Date.class, ContentItem.class.getDeclaredField("crawlDate").getType());
        assertEquals(LocalDateTime.class, ContentItem.class.getDeclaredField("crawlTime").getType());
        assertEquals(LocalDateTime.class, ContentItem.class.getDeclaredField("publishTime").getType());
        assertEquals(String.class, ContentItem.class.getDeclaredField("extraData").getType());
        assertEquals(String.class, ContentItem.class.getDeclaredField("contentType").getType());
        assertEquals(Integer.class, ContentItem.class.getDeclaredField("viewCount").getType());
    }

    @Test
    void githubTrendingDtoShouldExposeGithubSpecificFields() throws NoSuchFieldException {
        assertTrue(Serializable.class.isAssignableFrom(GithubTrendingItemDTO.class));

        assertEquals(String.class, GithubTrendingItemDTO.class.getDeclaredField("repoFullName").getType());
        assertEquals(String.class, GithubTrendingItemDTO.class.getDeclaredField("repoUrl").getType());
        assertEquals(String.class, GithubTrendingItemDTO.class.getDeclaredField("language").getType());
        assertEquals(Integer.class, GithubTrendingItemDTO.class.getDeclaredField("starCount").getType());
        assertEquals(Integer.class, GithubTrendingItemDTO.class.getDeclaredField("todayStarCount").getType());
    }

    @Test
    void generatorShouldTargetContentItemTable() throws NoSuchFieldException, IllegalAccessException {
        Field tableNamesField = MyBatisCodeGenerator.class.getDeclaredField("TABLE_NAMES");
        tableNamesField.setAccessible(true);
        String[] tableNames = (String[]) tableNamesField.get(null);
        assertArrayEquals(new String[]{"content_item"}, tableNames);
    }

    @Test
    void userProfileShouldBeMyBatisFlexEntityForUserProfileTable() throws NoSuchFieldException {
        Table table = UserProfile.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("user_profile", table.value());
        assertTrue(Serializable.class.isAssignableFrom(UserProfile.class));

        Field idField = UserProfile.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(String.class, UserProfile.class.getDeclaredField("nickname").getType());
        assertEquals(String.class, UserProfile.class.getDeclaredField("feishuUserId").getType());
        assertEquals(Integer.class, UserProfile.class.getDeclaredField("dailyPushLimit").getType());
        assertEquals(Integer.class, UserProfile.class.getDeclaredField("pushCooldownHours").getType());
        assertEquals(LocalDateTime.class, UserProfile.class.getDeclaredField("createTime").getType());
    }

    @Test
    void userKeywordSubscriptionShouldBeMyBatisFlexEntityForSubscriptionTable() throws NoSuchFieldException {
        Table table = UserKeywordSubscription.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("user_keyword_subscription", table.value());
        assertTrue(Serializable.class.isAssignableFrom(UserKeywordSubscription.class));

        Field idField = UserKeywordSubscription.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(Long.class, UserKeywordSubscription.class.getDeclaredField("userId").getType());
        assertEquals(String.class, UserKeywordSubscription.class.getDeclaredField("keyword").getType());
        assertEquals(Integer.class, UserKeywordSubscription.class.getDeclaredField("status").getType());
        assertEquals(LocalDateTime.class, UserKeywordSubscription.class.getDeclaredField("updateTime").getType());
    }

    @Test
    void userContentPushShouldBeMyBatisFlexEntityForPushTable() throws NoSuchFieldException {
        Table table = UserContentPush.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("user_content_push", table.value());
        assertTrue(Serializable.class.isAssignableFrom(UserContentPush.class));

        Field idField = UserContentPush.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(Long.class, UserContentPush.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, UserContentPush.class.getDeclaredField("contentItemId").getType());
        assertEquals(String.class, UserContentPush.class.getDeclaredField("pushChannel").getType());
        assertEquals(Integer.class, UserContentPush.class.getDeclaredField("pushStatus").getType());
        assertEquals(Integer.class, UserContentPush.class.getDeclaredField("queueStatus").getType());
        assertEquals(Integer.class, UserContentPush.class.getDeclaredField("retryCount").getType());
        assertEquals(Integer.class, UserContentPush.class.getDeclaredField("maxRetryCount").getType());
        assertEquals(LocalDateTime.class, UserContentPush.class.getDeclaredField("nextRetryTime").getType());
        assertEquals(LocalDateTime.class, UserContentPush.class.getDeclaredField("lastQueueTime").getType());
        assertEquals(String.class, UserContentPush.class.getDeclaredField("failReason").getType());
        assertEquals(LocalDateTime.class, UserContentPush.class.getDeclaredField("pushTime").getType());
    }

    @Test
    void crawlTaskLogShouldBeMyBatisFlexEntityForTaskLogTable() throws NoSuchFieldException {
        Table table = CrawlTaskLog.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("crawl_task_log", table.value());
        assertTrue(Serializable.class.isAssignableFrom(CrawlTaskLog.class));

        Field idField = CrawlTaskLog.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(String.class, CrawlTaskLog.class.getDeclaredField("taskType").getType());
        assertEquals(String.class, CrawlTaskLog.class.getDeclaredField("triggerSource").getType());
        assertEquals(Integer.class, CrawlTaskLog.class.getDeclaredField("taskStatus").getType());
        assertEquals(Integer.class, CrawlTaskLog.class.getDeclaredField("crawlCount").getType());
        assertEquals(Integer.class, CrawlTaskLog.class.getDeclaredField("enqueuedCount").getType());
        assertEquals(String.class, CrawlTaskLog.class.getDeclaredField("errorMessage").getType());
        assertEquals(LocalDateTime.class, CrawlTaskLog.class.getDeclaredField("startTime").getType());
        assertEquals(LocalDateTime.class, CrawlTaskLog.class.getDeclaredField("endTime").getType());
        assertEquals(Long.class, CrawlTaskLog.class.getDeclaredField("durationMs").getType());
    }

    @Test
    void userSubscriptionRuleShouldBeMyBatisFlexEntityForRuleTable() throws NoSuchFieldException {
        Table table = UserSubscriptionRule.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("user_subscription_rule", table.value());
        assertTrue(Serializable.class.isAssignableFrom(UserSubscriptionRule.class));

        Field idField = UserSubscriptionRule.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(Long.class, UserSubscriptionRule.class.getDeclaredField("userId").getType());
        assertEquals(String.class, UserSubscriptionRule.class.getDeclaredField("ruleType").getType());
        assertEquals(String.class, UserSubscriptionRule.class.getDeclaredField("ruleValue").getType());
        assertEquals(Integer.class, UserSubscriptionRule.class.getDeclaredField("ruleWeight").getType());
        assertEquals(Integer.class, UserSubscriptionRule.class.getDeclaredField("status").getType());
        assertEquals(LocalDateTime.class, UserSubscriptionRule.class.getDeclaredField("createTime").getType());
    }

    @Test
    void userSourceSubscriptionShouldBeMyBatisFlexEntityForSourceTable() throws NoSuchFieldException {
        Table table = UserSourceSubscription.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("user_source_subscription", table.value());
        assertTrue(Serializable.class.isAssignableFrom(UserSourceSubscription.class));

        Field idField = UserSourceSubscription.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);
        assertEquals(KeyType.Generator, id.keyType());

        assertEquals(Long.class, UserSourceSubscription.class.getDeclaredField("userId").getType());
        assertEquals(String.class, UserSourceSubscription.class.getDeclaredField("platform").getType());
        assertEquals(String.class, UserSourceSubscription.class.getDeclaredField("sourceType").getType());
        assertEquals(String.class, UserSourceSubscription.class.getDeclaredField("sourceValue").getType());
        assertEquals(Integer.class, UserSourceSubscription.class.getDeclaredField("status").getType());
    }
}
