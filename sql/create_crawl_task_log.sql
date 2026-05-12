use pingyu_info_diet;

create table if not exists crawl_task_log
(
    id                  bigint auto_increment comment '主键' primary key,
    taskType            varchar(64)                            not null comment '任务类型',
    triggerSource       varchar(32)                            not null default 'system' comment '触发来源',
    taskStatus          tinyint                                not null default 0 comment '任务状态 0-运行中 1-成功 2-失败',
    totalSourceCount    int                                    not null default 0 comment '处理订阅源数量',
    crawlCount          int                                    not null default 0 comment '抓取内容数量',
    savedCount          int                                    not null default 0 comment '新增入库数量',
    skippedCount        int                                    not null default 0 comment '跳过数量',
    matchedCount        int                                    not null default 0 comment '匹配数量',
    unmatchedCount      int                                    not null default 0 comment '未匹配数量',
    enqueuedCount       int                                    not null default 0 comment '成功入队数量',
    enqueueSkippedCount int                                    not null default 0 comment '入队跳过数量',
    errorMessage        varchar(1024)                          null comment '错误信息',
    startTime           datetime                               not null comment '开始时间',
    endTime             datetime                               null comment '结束时间',
    durationMs          bigint                                 null comment '耗时毫秒数',
    createTime          datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime          datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete            tinyint  default 0                     not null comment '是否删除',
    index idx_taskType_startTime (taskType, startTime),
    index idx_taskStatus_startTime (taskStatus, startTime)
) comment '采集任务日志表' collate = utf8mb4_unicode_ci;
