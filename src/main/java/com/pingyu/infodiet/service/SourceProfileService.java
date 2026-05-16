package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;

import java.util.List;

/**
 * 信源档案服务层。
 */
public interface SourceProfileService extends IService<SourceProfile> {

    /**
     * 根据订阅源解析或创建信源档案
     */
    SourceProfile resolveOrCreateBySubscription(UserSourceSubscription userSourceSubscription);

    /**
     * 根据内容信息解析或创建信源档案
     */
    SourceProfile resolveOrCreateByContent(
            String platform,
            String rawSourceType,
            String rawSourceValue,
            String sourceName,
            String sourceUrl
    );

    /**
     * 保存或更新信源档案
     */
    boolean saveOrUpdateSourceProfile(SourceProfile sourceProfile);

    /**
     * 查询启用信源档案
     */
    List<SourceProfile> listEnabledSourceProfiles();

    /**
     * 查询全部信源档案
     */
    List<SourceProfile> listAllSourceProfiles();
}
