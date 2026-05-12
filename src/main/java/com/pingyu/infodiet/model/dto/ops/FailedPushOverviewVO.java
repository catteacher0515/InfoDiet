package com.pingyu.infodiet.model.dto.ops;

import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 失败推送联动视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedPushOverviewVO {

    /**
     * 推送记录
     */
    private UserContentPush push;

    /**
     * 关联内容
     */
    private ContentItem contentItem;

    /**
     * 关联告警
     */
    private AlertRecord alertRecord;
}
