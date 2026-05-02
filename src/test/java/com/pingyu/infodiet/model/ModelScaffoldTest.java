package com.pingyu.infodiet.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.pingyu.infodiet.generator.MyBatisCodeGenerator;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
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
        assertEquals(String.class, ContentItem.class.getDeclaredField("extraData").getType());
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
}
