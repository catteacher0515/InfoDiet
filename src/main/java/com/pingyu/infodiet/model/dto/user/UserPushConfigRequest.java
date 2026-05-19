package com.pingyu.infodiet.model.dto.user;

import lombok.Data;

/**
 * 用户推送配置请求
 */
@Data
public class UserPushConfigRequest {

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
