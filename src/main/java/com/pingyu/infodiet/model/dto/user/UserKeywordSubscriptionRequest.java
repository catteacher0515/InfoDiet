package com.pingyu.infodiet.model.dto.user;

import lombok.Data;

/**
 * 用户关键词订阅请求
 */
@Data
public class UserKeywordSubscriptionRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 关键词
     */
    private String keyword;
}
