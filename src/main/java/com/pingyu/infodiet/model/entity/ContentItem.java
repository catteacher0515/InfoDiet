package com.pingyu.infodiet.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容抓取表 实体类。
 *
 * @author pingyu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("content_item")
public class ContentItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 来源平台
     */
    private String platform;

    /**
     * 平台内唯一标识
     */
    @Column("sourceId")
    private String sourceId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 内容链接
     */
    @Column("contentUrl")
    private String contentUrl;

    /**
     * 作者名或组织名
     */
    @Column("authorName")
    private String authorName;

    /**
     * 作者链接
     */
    @Column("authorUrl")
    private String authorUrl;

    /**
     * 项目语言
     */
    private String language;

    /**
     * 总 Star 数
     */
    @Column("starCount")
    private Integer starCount;

    /**
     * 今日新增 Star 数
     */
    @Column("todayStarCount")
    private Integer todayStarCount;

    /**
     * 是否命中关键词
     */
    @Column("keywordMatched")
    private Integer keywordMatched;

    /**
     * 推送状态 0-未推送 1-已推送
     */
    @Column("pushStatus")
    private Integer pushStatus;

    /**
     * 推送时间
     */
    @Column("pushTime")
    private LocalDateTime pushTime;

    /**
     * 扩展字段
     */
    @Column("extraData")
    private String extraData;

    /**
     * 抓取日期
     */
    @Column("crawlDate")
    private Date crawlDate;

    /**
     * 抓取时间
     */
    @Column("crawlTime")
    private LocalDateTime crawlTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
