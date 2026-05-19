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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 日报推送记录表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("daily_digest_push_record")
public class DailyDigestPushRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 日报日期
     */
    @Column("digestDate")
    private LocalDate digestDate;

    /**
     * 日报标题
     */
    @Column("digestTitle")
    private String digestTitle;

    /**
     * 用户 ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 推送渠道
     */
    @Column("pushChannel")
    private String pushChannel;

    /**
     * 接收人 ID
     */
    @Column("receiveId")
    private String receiveId;

    /**
     * 推送状态 1-成功 2-失败
     */
    @Column("pushStatus")
    private Integer pushStatus;

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
