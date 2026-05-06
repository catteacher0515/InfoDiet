package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.GithubTrendingService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Trending 抓取服务实现
 */
@Service
public class GithubTrendingServiceImpl implements GithubTrendingService {

    private static final String GITHUB_TRENDING_URL = "https://github.com/trending";

    @Override
    // 请求 GitHub Trending 页面
    // 拿到 HTML 并交给解析方法处理
    public List<GithubTrendingItemDTO> crawlGitHubTrending() {
        try {
            Document document = Jsoup.connect(GITHUB_TRENDING_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            return parseTrendingHtml(document.html());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取 GitHub Trending 失败");
        }
    }

    public List<GithubTrendingItemDTO> parseTrendingHtml(String html) {
        if (StrUtil.isBlank(html)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML 不能为空");
        }
        Document document = Jsoup.parse(html);
        Elements articles = document.select("article.Box-row");
        List<GithubTrendingItemDTO> result = new ArrayList<>();
        for (Element article : articles) {
            Element linkElement = article.selectFirst("h2 a");
            if (linkElement == null) {
                continue;
            }
            String href = linkElement.attr("href").trim();
            String repoFullName = normalizeRepoFullName(linkElement.text());
            String[] repoParts = repoFullName.split("/");
            String authorName = repoParts.length > 0 ? repoParts[0] : "";
            String repoName = repoParts.length > 1 ? repoParts[1] : repoFullName;

            GithubTrendingItemDTO item = new GithubTrendingItemDTO();
            item.setRepoFullName(repoFullName);
            item.setRepoName(repoName);
            item.setRepoUrl("https://github.com" + href);
            item.setAuthorName(authorName);
            item.setAuthorUrl(StrUtil.isBlank(authorName) ? null : "https://github.com/" + authorName);

            Element descriptionElement = article.selectFirst("p");
            item.setDescription(descriptionElement == null ? "" : cleanText(descriptionElement.text()));

            Element languageElement = article.selectFirst("span[itemprop=programmingLanguage]");
            item.setLanguage(languageElement == null ? "" : cleanText(languageElement.text()));

            Elements links = article.select("a[href$=/stargazers]");
            Element starElement = links.isEmpty() ? null : links.first();
            item.setStarCount(parseCount(starElement == null ? "0" : starElement.text()));

            String articleText = cleanText(article.text());
            item.setTodayStarCount(parseTodayStarCount(articleText));
            result.add(item);
        }
        return result;
    }

    private String normalizeRepoFullName(String repoText) {
        return cleanText(repoText).replace(" / ", "/");
    }

    private String cleanText(String text) {
        return StrUtil.blankToDefault(text, "").replaceAll("\\s+", " ").trim();
    }

    private Integer parseCount(String countText) {
        String normalized = cleanText(countText).replace(",", "");
        if (StrUtil.isBlank(normalized)) {
            return 0;
        }
        return Integer.parseInt(normalized);
    }

    private Integer parseTodayStarCount(String articleText) {
        String normalized = articleText.replace(",", "");
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s+stars?\\s+today").matcher(normalized);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
