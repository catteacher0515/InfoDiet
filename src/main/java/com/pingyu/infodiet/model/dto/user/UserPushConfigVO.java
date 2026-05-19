package com.pingyu.infodiet.model.dto.user;

import lombok.Builder;
import lombok.Data;

/**
 * 用户推送配置视图
 */
@Data
@Builder
public class UserPushConfigVO {

    /**
     * 飞书用户 ID
     */
    private String feishuUserId;

    /**
     * 推送渠道
     */
    private String pushChannel;

    /**
     * 每日推送上限
     */
    private Integer dailyPushLimit;

    /**
     * 推送冷却小时数
     */
    private Integer pushCooldownHours;
}
