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
 * 用户内容推送表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_content_push")
public class UserContentPush implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户 ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 内容 ID
     */
    @Column("contentItemId")
    private Long contentItemId;

    /**
     * 推送渠道
     */
    @Column("pushChannel")
    private String pushChannel;

    /**
     * 推送状态 0-待推送 1-推送成功 2-推送失败
     */
    @Column("pushStatus")
    private Integer pushStatus;

    /**
     * 队列状态 0-待入队 1-已入队 2-消费中 3-已完成
     */
    @Column("queueStatus")
    private Integer queueStatus;

    /**
     * 已重试次数
     */
    @Column("retryCount")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @Column("maxRetryCount")
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    @Column("nextRetryTime")
    private LocalDateTime nextRetryTime;

    /**
     * 最近入队时间
     */
    @Column("lastQueueTime")
    private LocalDateTime lastQueueTime;

    /**
     * 推送时间
     */
    @Column("pushTime")
    private LocalDateTime pushTime;

    /**
     * 失败原因
     */
    @Column("failReason")
    private String failReason;

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
