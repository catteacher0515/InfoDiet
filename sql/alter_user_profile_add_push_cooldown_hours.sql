alter table user_profile
    add column pushCooldownHours int not null default 0 comment '推送冷却小时数'
    after dailyPushLimit;
