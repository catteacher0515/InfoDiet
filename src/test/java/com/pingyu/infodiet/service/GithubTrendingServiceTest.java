package com.pingyu.infodiet.service;

import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.impl.GithubTrendingServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubTrendingServiceTest {

    private final GithubTrendingServiceImpl githubTrendingService = new GithubTrendingServiceImpl();

    @Test
    void parseTrendingHtmlShouldExtractRepositoryCards() {
        String html = """
                <article class="Box-row">
                  <h2>
                    <a href="/openai/openai-java">
                      openai / openai-java
                    </a>
                  </h2>
                  <p>
                    Java library for the OpenAI API
                  </p>
                  <div>
                    <span itemprop="programmingLanguage">Java</span>
                    <a href="/openai/openai-java/stargazers">1,234</a>
                    <span>345 stars today</span>
                  </div>
                </article>
                """;

        List<GithubTrendingItemDTO> items = githubTrendingService.parseTrendingHtml(html);

        assertEquals(1, items.size());
        GithubTrendingItemDTO item = items.get(0);
        assertEquals("openai/openai-java", item.getRepoFullName());
        assertEquals("openai-java", item.getRepoName());
        assertEquals("https://github.com/openai/openai-java", item.getRepoUrl());
        assertEquals("Java library for the OpenAI API", item.getDescription());
        assertEquals("openai", item.getAuthorName());
        assertEquals("https://github.com/openai", item.getAuthorUrl());
        assertEquals("Java", item.getLanguage());
        assertEquals(1234, item.getStarCount());
        assertEquals(345, item.getTodayStarCount());
    }

    @Test
    void parseTrendingHtmlShouldRejectBlankHtml() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> githubTrendingService.parseTrendingHtml(" "));

        assertTrue(exception.getMessage().contains("HTML"));
    }
}
