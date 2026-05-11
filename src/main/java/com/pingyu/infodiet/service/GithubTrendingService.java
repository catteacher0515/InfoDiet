package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;

import java.util.List;

/**
 * GitHub Trending 抓取服务
 */
public interface GithubTrendingService {

    /**
     * 抓取 GitHub Trending 仓库列表
     *
     * @return 仓库列表
     */
    List<GithubTrendingItemDTO> crawlGitHubTrending();

    /**
     * 抓取单个 GitHub 仓库信息
     *
     * @param repoFullName 仓库全名
     * @return 仓库信息
     */
    GithubTrendingItemDTO crawlGitHubRepo(String repoFullName);

    /**
     * 抓取 GitHub 作者或组织的仓库列表
     *
     * @param authorName 作者或组织名
     * @return 仓库列表
     */
    List<GithubTrendingItemDTO> crawlGitHubAuthorRepositories(String authorName);
}
