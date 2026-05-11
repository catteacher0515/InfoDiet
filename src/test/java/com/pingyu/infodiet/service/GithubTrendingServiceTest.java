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

    @Test
    void parseRepositoryHtmlShouldExtractSingleRepository() {
        String html = """
                <p class="f4 my-3">
                  The official Java library for the OpenAI API
                </p>
                <span itemprop="programmingLanguage">Java</span>
                <a href="/openai/openai-java/stargazers">1.5k</a>
                """;

        GithubTrendingItemDTO item = githubTrendingService.parseRepositoryHtml(html, "openai/openai-java");

        assertEquals("openai/openai-java", item.getRepoFullName());
        assertEquals("openai-java", item.getRepoName());
        assertEquals("https://github.com/openai/openai-java", item.getRepoUrl());
        assertEquals("openai", item.getAuthorName());
        assertEquals("https://github.com/openai", item.getAuthorUrl());
        assertEquals("The official Java library for the OpenAI API", item.getDescription());
        assertEquals("Java", item.getLanguage());
        assertEquals(1500, item.getStarCount());
        assertEquals(0, item.getTodayStarCount());
    }

    @Test
    void parseAuthorRepositoriesHtmlShouldExtractRepositoryList() {
        String html = """
                <div id="user-repositories-list">
                  <ul>
                    <li>
                      <a itemprop="name codeRepository" href="/openai/openai-java">openai-java</a>
                      <p itemprop="description">The official Java library for the OpenAI API</p>
                      <span itemprop="programmingLanguage">Java</span>
                      <a href="/openai/openai-java/stargazers">2,345</a>
                    </li>
                    <li>
                      <a itemprop="name codeRepository" href="/openai/codex">codex</a>
                      <p itemprop="description">Lightweight coding agent that runs in your terminal</p>
                      <span itemprop="programmingLanguage">Rust</span>
                      <a href="/openai/codex/stargazers">81.7k</a>
                    </li>
                  </ul>
                </div>
                """;

        List<GithubTrendingItemDTO> items = githubTrendingService.parseAuthorRepositoriesHtml(html, "openai");

        assertEquals(2, items.size());
        assertEquals("openai/openai-java", items.get(0).getRepoFullName());
        assertEquals(2345, items.get(0).getStarCount());
        assertEquals("openai/codex", items.get(1).getRepoFullName());
        assertEquals(81700, items.get(1).getStarCount());
        assertEquals("Rust", items.get(1).getLanguage());
    }
}
