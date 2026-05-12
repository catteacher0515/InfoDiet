alter table user_profile
    add column username varchar(64) null comment '登录账号' after nickname,
    add column password varchar(255) null comment '登录密码' after username,
    add column role varchar(32) null comment '角色' after password;

update user_profile
set username = concat('legacy_user_', id)
where username is null or trim(username) = '';

update user_profile
set password = '$2a$10$KfFpa8nMqCEvyc3aSTHQiuJa0/kDQh5rXHkQxC9DXCJE2FDjFfN2K'
where password is null or trim(password) = '';

update user_profile
set role = 'admin'
where id = (select min_id
            from (select min(id) as min_id from user_profile) temp);

update user_profile
set role = 'user'
where (role is null or trim(role) = '')
  and id <> (select min_id
             from (select min(id) as min_id from user_profile) temp);

alter table user_profile
    modify column username varchar(64) not null comment '登录账号',
    modify column password varchar(255) not null comment '登录密码',
    modify column role varchar(32) not null default 'user' comment '角色';

alter table user_profile
    add unique key uk_username (username);
