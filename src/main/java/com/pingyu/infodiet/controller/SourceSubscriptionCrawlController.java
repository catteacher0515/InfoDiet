package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.service.SourceSubscriptionCrawlService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订阅源采集接口
 */
@RestController
@RequestMapping("/source/crawl")
public class SourceSubscriptionCrawlController {

    @Resource
    private SourceSubscriptionCrawlService sourceSubscriptionCrawlService;

    /**
     * 手动触发 YouTube 订阅源采集
     */
    @PostMapping("/youtube/run")
    public BaseResponse<SourceSubscriptionCrawlService.CrawlResult> crawlYoutubeSourceSubscriptions() {
        return ResultUtils.success(sourceSubscriptionCrawlService.crawlYoutubeSourceSubscriptions());
    }

    /**
     * 手动触发 GitHub 订阅源采集
     */
    @PostMapping("/github/run")
    public BaseResponse<SourceSubscriptionCrawlService.CrawlResult> crawlGithubSourceSubscriptions() {
        return ResultUtils.success(sourceSubscriptionCrawlService.crawlGithubSourceSubscriptions());
    }

    /**
     * 手动触发全部订阅源采集
     */
    @PostMapping("/all/run")
    public BaseResponse<SourceSubscriptionCrawlService.CrawlResult> crawlAllSourceSubscriptions() {
        return ResultUtils.success(sourceSubscriptionCrawlService.crawlAllSourceSubscriptions());
    }
}
