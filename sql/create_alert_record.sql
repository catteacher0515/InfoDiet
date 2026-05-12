use pingyu_info_diet;

create table if not exists alert_record
(
    id             bigint auto_increment comment '主键' primary key,
    alertType      varchar(64)                            not null comment '告警类型',
    alertLevel     varchar(32)                            not null comment '告警级别',
    sourceType     varchar(64)                            not null comment '来源类型',
    sourceId       bigint                                 null comment '来源业务 ID',
    alertStatus    tinyint                                not null default 0 comment '告警状态 0-待处理 1-已发送 2-发送失败',
    alertTitle     varchar(256)                           not null comment '告警标题',
    alertContent   varchar(2048)                          not null comment '告警内容',
    failReason     varchar(512)                           null comment '发送失败原因',
    firstOccurTime datetime                               not null comment '首次发生时间',
    lastOccurTime  datetime                               not null comment '最近发生时间',
    sendTime       datetime                               null comment '发送时间',
    createTime     datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                     not null comment '是否删除',
    unique key uk_alert_source (alertType, sourceType, sourceId),
    index idx_alertStatus_lastOccurTime (alertStatus, lastOccurTime)
) comment '失败告警记录表' collate = utf8mb4_unicode_ci;
