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
 * 失败告警记录表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("alert_record")
public class AlertRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 告警类型
     */
    @Column("alertType")
    private String alertType;

    /**
     * 告警级别
     */
    @Column("alertLevel")
    private String alertLevel;

    /**
     * 来源类型
     */
    @Column("sourceType")
    private String sourceType;

    /**
     * 来源业务 ID
     */
    @Column("sourceId")
    private Long sourceId;

    /**
     * 告警状态 0-待处理 1-已发送 2-发送失败
     */
    @Column("alertStatus")
    private Integer alertStatus;

    /**
     * 告警标题
     */
    @Column("alertTitle")
    private String alertTitle;

    /**
     * 告警内容
     */
    @Column("alertContent")
    private String alertContent;

    /**
     * 发送失败原因
     */
    @Column("failReason")
    private String failReason;

    /**
     * 首次发生时间
     */
    @Column("firstOccurTime")
    private LocalDateTime firstOccurTime;

    /**
     * 最近发生时间
     */
    @Column("lastOccurTime")
    private LocalDateTime lastOccurTime;

    /**
     * 发送时间
     */
    @Column("sendTime")
    private LocalDateTime sendTime;

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
