package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 订阅匹配服务
 */
public interface SubscriptionMatchService {

    /**
     * 匹配启用用户的订阅内容
     */
    Map<Long, List<ContentItem>> matchEnabledUsers();

    /**
     * 匹配启用用户的订阅内容明细
     */
    Map<Long, List<MatchDetail>> matchEnabledUsersWithDetails();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class MatchDetail {

        /**
         * 命中内容
         */
        private ContentItem contentItem;

        /**
         * 匹配分数
         */
        private int score;

        /**
         * 命中规则
         */
        private List<String> matchedRules;
    }
}
