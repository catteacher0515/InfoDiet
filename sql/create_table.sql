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
    preFilterStatus tinyint                               default 0 not null comment '预筛状态 0-待预筛 1-通过 2-过滤',
    preFilterReason varchar(512)                          null comment '预筛结果说明',
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
    index idx_keywordMatched_pushStatus (keywordMatched, pushStatus),
    index idx_preFilterStatus_pushStatus (preFilterStatus, pushStatus)
    ) comment '内容抓取表' collate = utf8mb4_unicode_ci;


create table if not exists user_profile
(
    id             bigint auto_increment comment '主键' primary key,
    nickname       varchar(128)                           not null comment '用户昵称',
    username       varchar(64)                            not null comment '登录账号',
    password       varchar(255)                           not null comment '登录密码',
    role           varchar(32)                            not null default 'user' comment '角色',
    feishuUserId   varchar(128)                           null comment '飞书用户 ID',
    pushChannel    varchar(32)                            not null default 'feishu' comment '推送渠道',
    dailyPushLimit int                                    not null default 10 comment '每日推送上限',
    pushCooldownHours int                                 not null default 0 comment '推送冷却小时数',
    status         tinyint                                not null default 1 comment '状态 0-禁用 1-启用',
    createTime     datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                     not null comment '是否删除',
    unique key uk_username (username),
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

create table if not exists user_subscription_rule
(
    id          bigint auto_increment comment '主键' primary key,
    userId      bigint                                 not null comment '用户 ID',
    ruleType    varchar(64)                            not null comment '规则类型',
    ruleValue   varchar(256)                           not null comment '规则值',
    ruleWeight  int                                    not null default 1 comment '规则权重',
    status      tinyint                                not null default 1 comment '状态 0-禁用 1-启用',
    createTime  datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                     not null comment '是否删除',
    unique key uk_user_rule (userId, ruleType, ruleValue),
    index idx_userId_status (userId, status),
    index idx_ruleType_value (ruleType, ruleValue)
) comment '用户订阅规则表' collate = utf8mb4_unicode_ci;

create table if not exists user_content_push
(
    id            bigint auto_increment comment '主键' primary key,
    userId        bigint                                 not null comment '用户 ID',
    contentItemId bigint                                 not null comment '内容 ID',
    pushChannel   varchar(32)                            not null comment '推送渠道',
    pushStatus    tinyint                                not null default 0 comment '推送状态 0-待推送 1-推送成功 2-推送失败',
    queueStatus   tinyint                                not null default 0 comment '队列状态 0-待入队 1-已入队 2-消费中 3-已完成',
    retryCount    int                                    not null default 0 comment '已重试次数',
    maxRetryCount int                                    not null default 3 comment '最大重试次数',
    nextRetryTime datetime                               null comment '下次重试时间',
    lastQueueTime datetime                               null comment '最近入队时间',
    pushTime      datetime                               null comment '推送时间',
    failReason    varchar(512)                           null comment '失败原因',
    createTime    datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                     not null comment '是否删除',
    unique key uk_user_content (userId, contentItemId),
    index idx_pushStatus_channel (pushStatus, pushChannel),
    index idx_userId_pushStatus (userId, pushStatus),
    index idx_queueStatus_retryTime (queueStatus, nextRetryTime)
) comment '用户内容推送表' collate = utf8mb4_unicode_ci;

create table if not exists user_source_subscription
(
    id          bigint auto_increment comment '主键' primary key,
    userId      bigint                                 not null comment '用户 ID',
    platform    varchar(32)                            not null comment '平台',
    sourceType  varchar(64)                            not null comment '订阅源类型',
    sourceValue varchar(256)                           not null comment '订阅源值',
    status      tinyint                                not null default 1 comment '状态 0-禁用 1-启用',
    createTime  datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                     not null comment '是否删除',
    unique key uk_user_source (userId, platform, sourceType, sourceValue),
    index idx_userId_status (userId, status),
    index idx_platform_sourceType (platform, sourceType)
) comment '用户订阅源表' collate = utf8mb4_unicode_ci;

create table if not exists crawl_task_log
(
    id                 bigint auto_increment comment '主键' primary key,
    taskType           varchar(64)                            not null comment '任务类型',
    triggerSource      varchar(32)                            not null default 'system' comment '触发来源',
    taskStatus         tinyint                                not null default 0 comment '任务状态 0-运行中 1-成功 2-失败',
    totalSourceCount   int                                    not null default 0 comment '处理订阅源数量',
    crawlCount         int                                    not null default 0 comment '抓取内容数量',
    savedCount         int                                    not null default 0 comment '新增入库数量',
    skippedCount       int                                    not null default 0 comment '跳过数量',
    matchedCount       int                                    not null default 0 comment '匹配数量',
    unmatchedCount     int                                    not null default 0 comment '未匹配数量',
    enqueuedCount      int                                    not null default 0 comment '成功入队数量',
    enqueueSkippedCount int                                   not null default 0 comment '入队跳过数量',
    errorMessage       varchar(1024)                          null comment '错误信息',
    startTime          datetime                               not null comment '开始时间',
    endTime            datetime                               null comment '结束时间',
    durationMs         bigint                                 null comment '耗时毫秒数',
    createTime         datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime         datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete           tinyint  default 0                     not null comment '是否删除',
    index idx_taskType_startTime (taskType, startTime),
    index idx_taskStatus_startTime (taskStatus, startTime)
) comment '采集任务日志表' collate = utf8mb4_unicode_ci;

create table if not exists alert_record
(
    id              bigint auto_increment comment '主键' primary key,
    alertType       varchar(64)                            not null comment '告警类型',
    alertLevel      varchar(32)                            not null comment '告警级别',
    sourceType      varchar(64)                            not null comment '来源类型',
    sourceId        bigint                                 null comment '来源业务 ID',
    alertStatus     tinyint                                not null default 0 comment '告警状态 0-待处理 1-已发送 2-发送失败',
    alertTitle      varchar(256)                           not null comment '告警标题',
    alertContent    varchar(2048)                          not null comment '告警内容',
    failReason      varchar(512)                           null comment '发送失败原因',
    firstOccurTime  datetime                               not null comment '首次发生时间',
    lastOccurTime   datetime                               not null comment '最近发生时间',
    sendTime        datetime                               null comment '发送时间',
    createTime      datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                     not null comment '是否删除',
    unique key uk_alert_source (alertType, sourceType, sourceId),
    index idx_alertStatus_lastOccurTime (alertStatus, lastOccurTime)
) comment '失败告警记录表' collate = utf8mb4_unicode_ci;
