package com.pingyu.infodiet.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 采集任务日志表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crawl_task_log")
public class CrawlTaskLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 任务类型
     */
    @Column("taskType")
    private String taskType;

    /**
     * 触发来源
     */
    @Column("triggerSource")
    private String triggerSource;

    /**
     * 任务状态
     */
    @Column("taskStatus")
    private Integer taskStatus;

    /**
     * 处理订阅源数量
     */
    @Column("totalSourceCount")
    private Integer totalSourceCount;

    /**
     * 抓取内容数量
     */
    @Column("crawlCount")
    private Integer crawlCount;

    /**
     * 新增入库数量
     */
    @Column("savedCount")
    private Integer savedCount;

    /**
     * 跳过数量
     */
    @Column("skippedCount")
    private Integer skippedCount;

    /**
     * 匹配数量
     */
    @Column("matchedCount")
    private Integer matchedCount;

    /**
     * 未匹配数量
     */
    @Column("unmatchedCount")
    private Integer unmatchedCount;

    /**
     * 成功入队数量
     */
    @Column("enqueuedCount")
    private Integer enqueuedCount;

    /**
     * 入队跳过数量
     */
    @Column("enqueueSkippedCount")
    private Integer enqueueSkippedCount;

    /**
     * 错误信息
     */
    @Column("errorMessage")
    private String errorMessage;

    /**
     * 开始时间
     */
    @Column("startTime")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column("endTime")
    private LocalDateTime endTime;

    /**
     * 耗时毫秒数
     */
    @Column("durationMs")
    private Long durationMs;

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
