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
 * AI 日报历史表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("daily_digest_history")
public class DailyDigestHistory implements Serializable {

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
     * 精选事件总数
     */
    @Column("totalClusterCount")
    private Integer totalClusterCount;

    /**
     * 精选内容总数
     */
    @Column("totalItemCount")
    private Integer totalItemCount;

    /**
     * 日报摘要
     */
    private String summary;

    /**
     * 日报完整内容
     */
    @Column("digestContent")
    private String digestContent;

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
