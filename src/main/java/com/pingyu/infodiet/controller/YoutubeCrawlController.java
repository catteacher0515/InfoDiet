package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.YoutubeCrawlService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * YouTube 抓取接口
 */
@RestController
@RequestMapping("/youtube")
public class YoutubeCrawlController {

    @Resource
    private YoutubeCrawlService youtubeCrawlService;

    @Resource
    private ContentItemService contentItemService;

    /**
     * 手动抓取 YouTube 视频
     */
    @GetMapping("/crawl")
    public BaseResponse<List<YoutubeVideoItemDTO>> crawlYoutubeVideos(@RequestParam String channelId) {
        return ResultUtils.success(youtubeCrawlService.crawlYoutubeVideos(channelId));
    }

    /**
     * 手动抓取并保存 YouTube 视频
     */
    @GetMapping("/crawl/save")
    public BaseResponse<ContentItemService.SaveResult> crawlAndSaveYoutubeVideos(@RequestParam String channelId) {
        List<YoutubeVideoItemDTO> dtoList = youtubeCrawlService.crawlYoutubeVideos(channelId);
        ContentItemService.SaveResult saveResult = contentItemService.saveYoutubeVideoItems(dtoList);
        return ResultUtils.success(saveResult);
    }
}
