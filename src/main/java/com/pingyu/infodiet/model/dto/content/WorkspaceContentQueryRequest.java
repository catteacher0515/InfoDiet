package com.pingyu.infodiet.model.dto.content;

import lombok.Data;

/**
 * 用户工作台内容查询
 */
@Data
public class WorkspaceContentQueryRequest {

    /**
     * 平台
     */
    private String platform;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 限制条数
     */
    private Integer limit;
}
