package com.pingyu.infodiet.model.dto.youtube;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * YouTube 视频抓取结果
 */
@Data
public class YoutubeVideoItemDTO implements Serializable {

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 频道 ID
     */
    private String channelId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频链接
     */
    private String videoUrl;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 作者名
     */
    private String authorName;

    /**
     * 作者链接
     */
    private String authorUrl;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    private static final long serialVersionUID = 1L;
}
