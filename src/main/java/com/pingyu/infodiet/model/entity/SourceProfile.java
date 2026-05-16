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
 * 信源档案表 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("source_profile")
public class SourceProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 平台
     */
    private String platform;

    /**
     * 档案类型
     */
    @Column("profileType")
    private String profileType;

    /**
     * 信源唯一键
     */
    @Column("sourceKey")
    private String sourceKey;

    /**
     * 信源名称
     */
    @Column("sourceName")
    private String sourceName;

    /**
     * 信源链接
     */
    @Column("sourceUrl")
    private String sourceUrl;

    /**
     * 信源分类
     */
    @Column("sourceCategory")
    private String sourceCategory;

    /**
     * 信源等级
     */
    @Column("sourceTier")
    private String sourceTier;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

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
