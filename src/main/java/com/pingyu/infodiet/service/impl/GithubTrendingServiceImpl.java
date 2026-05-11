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
    private static final String GITHUB_REPO_URL = "https://github.com/%s";
    private static final String GITHUB_AUTHOR_REPOSITORIES_URL = "https://github.com/%s?tab=repositories";

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

    /**
     * 抓取单个 GitHub 仓库
     */
    @Override
    public GithubTrendingItemDTO crawlGitHubRepo(String repoFullName) {
        String normalizedRepoFullName = normalizeRepoFullName(repoFullName);
        if (StrUtil.isBlank(normalizedRepoFullName) || !normalizedRepoFullName.contains("/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仓库全名不合法");
        }
        try {
            Document document = Jsoup.connect(String.format(GITHUB_REPO_URL, normalizedRepoFullName))
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            return parseRepositoryHtml(document.html(), normalizedRepoFullName);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取 GitHub 仓库失败");
        }
    }

    /**
     * 抓取 GitHub 作者或组织的仓库列表
     */
    @Override
    public List<GithubTrendingItemDTO> crawlGitHubAuthorRepositories(String authorName) {
        String normalizedAuthorName = cleanText(authorName);
        if (StrUtil.isBlank(normalizedAuthorName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "作者名不能为空");
        }
        try {
            Document document = Jsoup.connect(String.format(GITHUB_AUTHOR_REPOSITORIES_URL, normalizedAuthorName))
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            return parseAuthorRepositoriesHtml(document.html(), normalizedAuthorName);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取 GitHub 作者仓库失败");
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

    public GithubTrendingItemDTO parseRepositoryHtml(String html, String repoFullName) {
        if (StrUtil.isBlank(html)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML 不能为空");
        }
        String normalizedRepoFullName = normalizeRepoFullName(repoFullName);
        String[] repoParts = normalizedRepoFullName.split("/");
        if (repoParts.length < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仓库全名不合法");
        }
        String authorName = repoParts[0];
        String repoName = repoParts[1];
        Document document = Jsoup.parse(html);

        GithubTrendingItemDTO item = new GithubTrendingItemDTO();
        item.setRepoFullName(normalizedRepoFullName);
        item.setRepoName(repoName);
        item.setRepoUrl(String.format(GITHUB_REPO_URL, normalizedRepoFullName));
        item.setAuthorName(authorName);
        item.setAuthorUrl("https://github.com/" + authorName);

        Element descriptionElement = document.selectFirst("p.f4.my-3, p.f4");
        item.setDescription(descriptionElement == null ? "" : cleanText(descriptionElement.text()));

        Element languageElement = document.selectFirst("span[itemprop=programmingLanguage]");
        item.setLanguage(languageElement == null ? "" : cleanText(languageElement.text()));

        Element starElement = document.selectFirst("a[href$=/stargazers]");
        item.setStarCount(parseCount(starElement == null ? "0" : starElement.text()));
        item.setTodayStarCount(0);
        return item;
    }

    public List<GithubTrendingItemDTO> parseAuthorRepositoriesHtml(String html, String authorName) {
        if (StrUtil.isBlank(html)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML 不能为空");
        }
        String normalizedAuthorName = cleanText(authorName);
        if (StrUtil.isBlank(normalizedAuthorName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "作者名不能为空");
        }
        Document document = Jsoup.parse(html);
        Elements repositories = document.select("#user-repositories-list li, li[itemprop=owns]");
        List<GithubTrendingItemDTO> result = new ArrayList<>();
        for (Element repository : repositories) {
            Element linkElement = repository.selectFirst("a[itemprop=name codeRepository], a[href^=/" + normalizedAuthorName + "/]");
            if (linkElement == null) {
                continue;
            }
            String href = cleanText(linkElement.attr("href"));
            if (StrUtil.isBlank(href) || !href.startsWith("/")) {
                continue;
            }
            String repoFullName = href.substring(1);
            if (!repoFullName.contains("/")) {
                continue;
            }
            String repoName = repoFullName.substring(repoFullName.indexOf("/") + 1);

            GithubTrendingItemDTO item = new GithubTrendingItemDTO();
            item.setRepoFullName(repoFullName);
            item.setRepoName(repoName);
            item.setRepoUrl("https://github.com" + href);
            item.setAuthorName(normalizedAuthorName);
            item.setAuthorUrl("https://github.com/" + normalizedAuthorName);

            Element descriptionElement = repository.selectFirst("p[itemprop=description], p.col-9");
            item.setDescription(descriptionElement == null ? "" : cleanText(descriptionElement.text()));

            Element languageElement = repository.selectFirst("span[itemprop=programmingLanguage]");
            item.setLanguage(languageElement == null ? "" : cleanText(languageElement.text()));

            Element starElement = repository.selectFirst("a[href$=/stargazers]");
            item.setStarCount(parseCount(starElement == null ? "0" : starElement.text()));
            item.setTodayStarCount(0);
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
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)([kKmM]?)")
                .matcher(normalized);
        if (!matcher.find()) {
            return 0;
        }
        double number = Double.parseDouble(matcher.group(1));
        String suffix = matcher.group(2).toLowerCase();
        if ("k".equals(suffix)) {
            return (int) Math.round(number * 1000);
        }
        if ("m".equals(suffix)) {
            return (int) Math.round(number * 1_000_000);
        }
        return (int) Math.round(number);
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
