package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.service.YoutubeCrawlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * YouTube 抓取服务实现
 */
@Service
public class YoutubeCrawlServiceImpl implements YoutubeCrawlService {

    private static final String YOUTUBE_CHANNEL_FEED_URL =
            "https://www.youtube.com/feeds/videos.xml?channel_id=%s";

    /**
     * 抓取 YouTube 频道视频列表
     */
    @Override
    public List<YoutubeVideoItemDTO> crawlYoutubeVideos(String channelId) {
        if (StrUtil.isBlank(channelId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "频道 ID 不能为空");
        }
        try {
            Document document = Jsoup.connect(String.format(YOUTUBE_CHANNEL_FEED_URL, channelId))
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .ignoreContentType(true)
                    .get();
            return parseChannelFeed(document.outerHtml());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取 YouTube 频道失败");
        }
    }

    public List<YoutubeVideoItemDTO> parseChannelFeed(String xml) {
        if (StrUtil.isBlank(xml)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "XML 不能为空");
        }
        Document document = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements entries = document.select("entry");
        List<YoutubeVideoItemDTO> result = new ArrayList<>();
        for (Element entry : entries) {
            String videoId = cleanText(entry.selectFirst("videoId") == null
                    ? entry.selectFirst("yt|videoId") == null ? "" : entry.selectFirst("yt|videoId").text()
                    : entry.selectFirst("videoId").text());
            if (StrUtil.isBlank(videoId)) {
                continue;
            }

            YoutubeVideoItemDTO item = new YoutubeVideoItemDTO();
            item.setVideoId(videoId);
            item.setChannelId(cleanText(entry.selectFirst("channelId") == null
                    ? entry.selectFirst("yt|channelId") == null ? "" : entry.selectFirst("yt|channelId").text()
                    : entry.selectFirst("channelId").text()));
            item.setTitle(cleanText(entry.selectFirst("title") == null ? "" : entry.selectFirst("title").text()));

            Element linkElement = entry.selectFirst("link[href]");
            item.setVideoUrl(linkElement == null ? buildVideoUrl(videoId) : linkElement.attr("href"));

            Element authorNameElement = entry.selectFirst("author > name");
            item.setAuthorName(authorNameElement == null ? "" : cleanText(authorNameElement.text()));
            Element authorUrlElement = entry.selectFirst("author > uri");
            item.setAuthorUrl(authorUrlElement == null ? "" : cleanText(authorUrlElement.text()));

            Element descriptionElement = entry.selectFirst("group > description, media|group > media|description");
            item.setDescription(descriptionElement == null ? "" : cleanText(descriptionElement.text()));

            Element publishedElement = entry.selectFirst("published");
            item.setPublishTime(parsePublishTime(publishedElement == null ? "" : publishedElement.text()));
            result.add(item);
        }
        return result;
    }

    private String buildVideoUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private LocalDateTime parsePublishTime(String publishTimeText) {
        if (StrUtil.isBlank(publishTimeText)) {
            return null;
        }
        return OffsetDateTime.parse(cleanText(publishTimeText)).toLocalDateTime();
    }

    private String cleanText(String text) {
        return StrUtil.blankToDefault(text, "").replaceAll("\\s+", " ").trim();
    }
}
