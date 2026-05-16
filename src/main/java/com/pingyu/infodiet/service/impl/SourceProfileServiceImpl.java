package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.SourceProfileMapper;
import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.SourceProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 信源档案服务实现。
 */
@Service
public class SourceProfileServiceImpl extends ServiceImpl<SourceProfileMapper, SourceProfile>
        implements SourceProfileService {

    private static final String DEFAULT_SOURCE_CATEGORY = "normal";
    private static final String DEFAULT_SOURCE_TIER = "T2";

    /**
     * 根据订阅源解析或创建信源档案
     */
    @Override
    public SourceProfile resolveOrCreateBySubscription(UserSourceSubscription userSourceSubscription) {
        if (userSourceSubscription == null) {
            return null;
        }
        SourceDescriptor descriptor = buildDescriptorBySubscription(userSourceSubscription);
        if (descriptor == null || StrUtil.hasBlank(descriptor.platform(), descriptor.profileType(), descriptor.sourceKey())) {
            return null;
        }
        return resolveOrCreate(
                descriptor.platform(),
                descriptor.profileType(),
                descriptor.sourceKey(),
                descriptor.sourceName(),
                descriptor.sourceUrl()
        );
    }

    /**
     * 根据内容信息解析或创建信源档案
     */
    @Override
    public SourceProfile resolveOrCreateByContent(
            String platform,
            String rawSourceType,
            String rawSourceValue,
            String sourceName,
            String sourceUrl
    ) {
        String normalizedPlatform = normalize(platform);
        String normalizedSourceType = normalize(rawSourceType);
        String normalizedSourceValue = StrUtil.trim(rawSourceValue);
        if (StrUtil.hasBlank(normalizedPlatform, normalizedSourceType, normalizedSourceValue)) {
            return null;
        }
        return resolveOrCreate(
                normalizedPlatform,
                normalizedSourceType,
                normalizedSourceValue,
                StrUtil.trim(sourceName),
                StrUtil.trim(sourceUrl)
        );
    }

    /**
     * 保存或更新信源档案
     */
    @Override
    public boolean saveOrUpdateSourceProfile(SourceProfile sourceProfile) {
        if (sourceProfile == null) {
            return false;
        }
        sourceProfile.setPlatform(normalize(sourceProfile.getPlatform()));
        sourceProfile.setProfileType(normalize(sourceProfile.getProfileType()));
        sourceProfile.setSourceKey(StrUtil.trim(sourceProfile.getSourceKey()));
        sourceProfile.setSourceName(StrUtil.trim(sourceProfile.getSourceName()));
        sourceProfile.setSourceUrl(StrUtil.trim(sourceProfile.getSourceUrl()));
        sourceProfile.setSourceCategory(defaultIfBlank(sourceProfile.getSourceCategory(), DEFAULT_SOURCE_CATEGORY));
        sourceProfile.setSourceTier(defaultIfBlank(sourceProfile.getSourceTier(), DEFAULT_SOURCE_TIER));
        if (sourceProfile.getStatus() == null) {
            sourceProfile.setStatus(1);
        }
        if (sourceProfile.getId() == null) {
            return this.save(sourceProfile);
        }
        return this.updateById(sourceProfile);
    }

    /**
     * 查询启用信源档案
     */
    @Override
    public List<SourceProfile> listEnabledSourceProfiles() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", 1)
                .orderBy("updateTime", false);
        return this.list(queryWrapper);
    }

    /**
     * 查询全部信源档案
     */
    @Override
    public List<SourceProfile> listAllSourceProfiles() {
        return this.list(QueryWrapper.create().orderBy("updateTime", false));
    }

    /**
     * 解析或创建信源档案
     */
    protected SourceProfile resolveOrCreate(
            String platform,
            String profileType,
            String sourceKey,
            String sourceName,
            String sourceUrl
    ) {
        SourceProfile existing = getByPlatformAndProfileTypeAndSourceKey(platform, profileType, sourceKey);
        if (existing != null) {
            if (shouldRefreshSourceMeta(existing, sourceName, sourceUrl)) {
                SourceProfile updateRecord = new SourceProfile();
                updateRecord.setId(existing.getId());
                updateRecord.setSourceName(StrUtil.blankToDefault(StrUtil.trim(sourceName), existing.getSourceName()));
                updateRecord.setSourceUrl(StrUtil.blankToDefault(StrUtil.trim(sourceUrl), existing.getSourceUrl()));
                this.updateById(updateRecord);
                existing.setSourceName(updateRecord.getSourceName());
                existing.setSourceUrl(updateRecord.getSourceUrl());
            }
            return existing;
        }
        SourceProfile sourceProfile = SourceProfile.builder()
                .platform(platform)
                .profileType(profileType)
                .sourceKey(sourceKey)
                .sourceName(StrUtil.blankToDefault(StrUtil.trim(sourceName), sourceKey))
                .sourceUrl(StrUtil.trim(sourceUrl))
                .sourceCategory(DEFAULT_SOURCE_CATEGORY)
                .sourceTier(DEFAULT_SOURCE_TIER)
                .status(1)
                .build();
        this.save(sourceProfile);
        return sourceProfile;
    }

    /**
     * 按平台、档案类型和唯一键查询信源档案
     */
    protected SourceProfile getByPlatformAndProfileTypeAndSourceKey(String platform, String profileType, String sourceKey) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("platform", platform)
                .eq("profileType", profileType)
                .eq("sourceKey", sourceKey)
                .limit(1);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据订阅源构建信源描述
     */
    protected SourceDescriptor buildDescriptorBySubscription(UserSourceSubscription userSourceSubscription) {
        String platform = normalize(userSourceSubscription.getPlatform());
        String sourceType = normalize(userSourceSubscription.getSourceType());
        String sourceValue = StrUtil.trim(userSourceSubscription.getSourceValue());
        if (StrUtil.hasBlank(platform, sourceType, sourceValue)) {
            return null;
        }
        if ("youtube".equals(platform) && "channel".equals(sourceType)) {
            return new SourceDescriptor(platform, "channel", sourceValue, sourceValue, buildYoutubeChannelUrl(sourceValue));
        }
        if ("github".equals(platform) && "author".equals(sourceType)) {
            return new SourceDescriptor(platform, "author", sourceValue, sourceValue, buildGithubAuthorUrl(sourceValue));
        }
        if ("github".equals(platform) && "repo".equals(sourceType)) {
            String authorName = extractGithubAuthor(sourceValue);
            if (StrUtil.isBlank(authorName)) {
                return null;
            }
            return new SourceDescriptor(platform, "author", authorName, authorName, buildGithubAuthorUrl(authorName));
        }
        return new SourceDescriptor(platform, sourceType, sourceValue, sourceValue, null);
    }

    /**
     * 判断是否需要刷新信源元信息
     */
    protected boolean shouldRefreshSourceMeta(SourceProfile sourceProfile, String sourceName, String sourceUrl) {
        return sourceProfile != null && (
                (StrUtil.isBlank(sourceProfile.getSourceName()) && StrUtil.isNotBlank(sourceName))
                        || (StrUtil.isBlank(sourceProfile.getSourceUrl()) && StrUtil.isNotBlank(sourceUrl))
        );
    }

    /**
     * 提取 GitHub 作者名
     */
    protected String extractGithubAuthor(String repoFullName) {
        String normalizedValue = StrUtil.trim(repoFullName);
        if (StrUtil.isBlank(normalizedValue) || !normalizedValue.contains("/")) {
            return null;
        }
        return normalize(StrUtil.subBefore(normalizedValue, "/", false));
    }

    /**
     * 构建 GitHub 作者链接
     */
    protected String buildGithubAuthorUrl(String authorName) {
        return StrUtil.isBlank(authorName) ? null : "https://github.com/" + StrUtil.trim(authorName);
    }

    /**
     * 构建 YouTube 频道链接
     */
    protected String buildYoutubeChannelUrl(String channelId) {
        return StrUtil.isBlank(channelId) ? null : "https://www.youtube.com/channel/" + StrUtil.trim(channelId);
    }

    /**
     * 标准化文本
     */
    protected String normalize(String value) {
        return StrUtil.trim(value).toLowerCase();
    }

    /**
     * 兜底空字符串
     */
    protected String defaultIfBlank(String value, String defaultValue) {
        return StrUtil.isBlank(value) ? defaultValue : StrUtil.trim(value);
    }

    /**
     * 信源描述
     */
    protected record SourceDescriptor(
            String platform,
            String profileType,
            String sourceKey,
            String sourceName,
            String sourceUrl
    ) {
    }
}
