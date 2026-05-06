package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.GithubTrendingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GitHub Trending 抓取接口
 */
@RestController
@RequestMapping("/github/trending")
public class GithubTrendingController {

    @Resource
    private GithubTrendingService githubTrendingService;

    /**
     * 手动抓取 GitHub Trending
     *
     * @return 抓取结果
     */
    @GetMapping("/crawl")
    public BaseResponse<List<GithubTrendingItemDTO>> crawlGitHubTrending() {
        return ResultUtils.success(githubTrendingService.crawlGitHubTrending());
    }
}
