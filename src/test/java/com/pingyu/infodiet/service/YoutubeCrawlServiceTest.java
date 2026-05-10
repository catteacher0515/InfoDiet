package com.pingyu.infodiet.service;

import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.service.impl.YoutubeCrawlServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YoutubeCrawlServiceTest {

    private final YoutubeCrawlServiceImpl youtubeCrawlService = new YoutubeCrawlServiceImpl();

    @Test
    void parseChannelFeedShouldExtractVideoEntries() {
        String xml = """
                <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015"
                      xmlns:media="http://search.yahoo.com/mrss/">
                  <entry>
                    <id>yt:video:video123</id>
                    <yt:videoId>video123</yt:videoId>
                    <yt:channelId>UC123456</yt:channelId>
                    <title>Build InfoDiet with Java</title>
                    <link rel="alternate" href="https://www.youtube.com/watch?v=video123"/>
                    <author>
                      <name>Pingyu Channel</name>
                      <uri>https://www.youtube.com/channel/UC123456</uri>
                    </author>
                    <published>2026-05-10T07:30:00+00:00</published>
                    <media:group>
                      <media:description>Build your own information diet system</media:description>
                    </media:group>
                  </entry>
                </feed>
                """;

        List<YoutubeVideoItemDTO> items = youtubeCrawlService.parseChannelFeed(xml);

        assertEquals(1, items.size());
        YoutubeVideoItemDTO item = items.getFirst();
        assertEquals("video123", item.getVideoId());
        assertEquals("UC123456", item.getChannelId());
        assertEquals("Build InfoDiet with Java", item.getTitle());
        assertEquals("https://www.youtube.com/watch?v=video123", item.getVideoUrl());
        assertEquals("Build your own information diet system", item.getDescription());
        assertEquals("Pingyu Channel", item.getAuthorName());
        assertEquals("https://www.youtube.com/channel/UC123456", item.getAuthorUrl());
        assertEquals(LocalDateTime.of(2026, 5, 10, 7, 30), item.getPublishTime());
    }

    @Test
    void parseChannelFeedShouldRejectBlankXml() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> youtubeCrawlService.parseChannelFeed(" "));

        assertTrue(exception.getMessage().contains("XML"));
    }
}
