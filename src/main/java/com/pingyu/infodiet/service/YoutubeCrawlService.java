package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;

import java.util.List;

/**
 * YouTube 抓取服务
 */
public interface YoutubeCrawlService {

    /**
     * 抓取 YouTube 频道视频列表
     *
     * @param channelId 频道 ID
     * @return 视频列表
     */
    List<YoutubeVideoItemDTO> crawlYoutubeVideos(String channelId);
}
