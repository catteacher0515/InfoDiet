package com.pingyu.infodiet.model.dto.github;

import lombok.Data;

import java.io.Serializable;

/**
 * GitHub Trending 抓取结果
 */
@Data
public class GithubTrendingItemDTO implements Serializable {

    /**
     * 仓库完整名，如 owner/repo
     */
    private String repoFullName;

    /**
     * 仓库名
     */
    private String repoName;

    /**
     * 仓库链接
     */
    private String repoUrl;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 作者名或组织名
     */
    private String authorName;

    /**
     * 作者主页链接
     */
    private String authorUrl;

    /**
     * 编程语言
     */
    private String language;

    /**
     * Star 总数
     */
    private Integer starCount;

    /**
     * 今日新增 Star 数
     */
    private Integer todayStarCount;

    private static final long serialVersionUID = 1L;
}
