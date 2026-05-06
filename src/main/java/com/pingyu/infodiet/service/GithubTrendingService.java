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
}
