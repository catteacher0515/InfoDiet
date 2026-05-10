create database if not exists pingyu_info_diet;
use pingyu_info_diet;

create table if not exists content_item
(
    id             bigint auto_increment comment '主键' primary key,
    platform       varchar(32)                            not null comment '来源平台',
    sourceId       varchar(128)                           not null comment '平台内唯一标识',
    title          varchar(512)                           not null comment '标题',
    contentType    varchar(64)                            null comment '内容类型',
    description    text                                   null comment '描述',
    contentUrl     varchar(1024)                          not null comment '内容链接',
    authorName     varchar(256)                           null comment '作者名或组织名',
    authorUrl      varchar(1024)                          null comment '作者链接',
    language       varchar(64)                            null comment '项目语言',
    starCount      int                                    default 0 not null comment '总 Star 数',
    todayStarCount int                                    default 0 not null comment '今日新增 Star 数',
    viewCount      int                                    default 0 not null comment '播放量',
    keywordMatched tinyint                                default 0 not null comment '是否命中关键词',
    pushStatus     tinyint                                default 0 not null comment '推送状态 0-未推送 1-已推送',
    pushTime       datetime                               null comment '推送时间',
    extraData      json                                   null comment '扩展字段',
    crawlDate      date                                   not null comment '抓取日期',
    crawlTime      datetime                               not null comment '抓取时间',
    publishTime    datetime                               null comment '原始发布时间',
    createTime     datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                     not null comment '是否删除',
    unique key uk_platform_source_date (platform, sourceId, crawlDate),
    index idx_platform_crawlDate (platform, crawlDate),
    index idx_keywordMatched_pushStatus (keywordMatched, pushStatus)
    ) comment '内容抓取表' collate = utf8mb4_unicode_ci;


create table if not exists user_profile
(
    id             bigint auto_increment comment '主键' primary key,
    nickname       varchar(128)                           not null comment '用户昵称',
    feishuUserId   varchar(128)                           null comment '飞书用户 ID',
    pushChannel    varchar(32)                            not null default 'feishu' comment '推送渠道',
    dailyPushLimit int                                    not null default 10 comment '每日推送上限',
    status         tinyint                                not null default 1 comment '状态 0-禁用 1-启用',
    createTime     datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                     not null comment '是否删除',
    unique key uk_feishuUserId (feishuUserId),
    index idx_status (status)
) comment '用户信息表' collate = utf8mb4_unicode_ci;

create table if not exists user_keyword_subscription
(
    id          bigint auto_increment comment '主键' primary key,
    userId      bigint                                 not null comment '用户 ID',
    keyword     varchar(128)                           not null comment '订阅关键词',
    status      tinyint                                not null default 1 comment '状态 0-禁用 1-启用',
    createTime  datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                     not null comment '是否删除',
    unique key uk_user_keyword (userId, keyword),
    index idx_userId_status (userId, status)
) comment '用户关键词订阅表' collate = utf8mb4_unicode_ci;
