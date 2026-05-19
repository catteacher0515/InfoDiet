# InfoDiet

InfoDiet 是一个面向“信息节食”的内容筛选与推送系统。当前工程已经包含：

- 后端 API
- 前端工作台
- 管理区与运维区
- RabbitMQ 异步推送
- 飞书日报 IM 推送
- 飞书 Base 内容推送

当前进度与验证节点见：[PROJECT_PROGRESS.md](./PROJECT_PROGRESS.md)

## 1. 本地依赖

本地联调默认依赖：

- Java `21`
- Maven Wrapper `./mvnw`
- Node.js `24+`
- `pnpm`
- MySQL `8.x`
- Redis `7.x`
- RabbitMQ `3.x`

默认本地配置见 [application.yml](./src/main/resources/application.yml)：

- 后端端口：`8123`
- 前端端口：`5173`
- MySQL：`localhost:3306`
- Redis：`localhost:6379`
- RabbitMQ：`localhost:5672`

## 2. 数据库初始化

先创建数据库：

```sql
create database if not exists pingyu_info_diet character set utf8mb4 collate utf8mb4_unicode_ci;
```

再导入表结构：

```bash
mysql -uroot -p112233665qw pingyu_info_diet < sql/create_table.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/create_alert_record.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/create_crawl_task_log.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/create_source_profile.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_content_item_add_pre_filter_fields.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_content_item_add_quality_score_fields.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_content_item_add_source_governance_fields.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_user_profile_add_auth_fields.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_user_profile_add_push_cooldown_hours.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_user_source_subscription_add_source_profile_id.sql
mysql -uroot -p112233665qw pingyu_info_diet < sql/alter_user_content_push_add_retry_and_queue_fields.sql
```

初始化演示账号：

```bash
mysql -uroot -p112233665qw pingyu_info_diet < sql/init_demo_accounts.sql
```

演示账号：

- 管理员：`demo_admin / Infodiet123!`
- 普通用户：`demo_user / Infodiet123!`

## 3. 中间件启动

### Redis

如果本地已安装：

```bash
redis-server
```

### RabbitMQ

推荐直接用 Docker：

```bash
docker run -d \
  --name infodiet-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

验证：

```bash
docker exec infodiet-rabbitmq rabbitmqctl list_queues name messages messages_ready messages_unacknowledged
```

## 4. 后端启动

在项目根目录执行：

```bash
./mvnw spring-boot:run
```

默认访问地址：

- API：`http://127.0.0.1:8123/api`

如果 `8123` 被旧进程占用：

```bash
lsof -iTCP:8123 -sTCP:LISTEN -n -P
kill <PID>
./mvnw spring-boot:run
```

## 5. 前端启动

```bash
cd frontend
pnpm install
pnpm dev
```

默认访问地址：

- Web：`http://127.0.0.1:5173`

生产构建验证：

```bash
cd frontend
pnpm build
```

## 6. 本地联调顺序

建议每次都按这个顺序回归：

1. 前后端本地启动是否正常
2. 登录与角色跳转是否正常
3. 用户区订阅、内容、推送记录是否正常
4. 管理区用户管理与订阅总览是否正常
5. 运维区任务日志、失败推送、失败告警是否正常
6. 日报运营页是否正常
7. RabbitMQ 异步用户内容推送是否正常
8. 最后执行全量测试

可直接参考：[docs/demo/2026-05-13-联调与演示-SOP.md](./docs/demo/2026-05-13-联调与演示-SOP.md)

## 7. 真实飞书回归

当前仓库内 `application.yml` 已包含飞书配置，真实验证时不要再用假的 `FEISHU_BASE_*` 环境变量覆盖。

### 7.1 日报 IM 推送

执行：

```bash
curl -s -X POST http://127.0.0.1:8123/api/feishu/push/digest/today
```

成功判定：

- 返回 `code=0`
- `daily_digest_push_record` 新增成功记录
- 飞书 IM 收到今日日报

### 7.2 `user_content_push` 异步内容推送

执行：

```bash
curl -s -X POST http://127.0.0.1:8123/api/user-content-push/enqueue
```

成功判定：

- 返回 `code=0`
- RabbitMQ 队列最终清空
- `user_content_push` 目标记录变为：
  - `pushStatus=1`
  - `queueStatus=3`
- 飞书 Base 新增对应内容记录

## 8. 历史坏样本清理

历史联调曾留下指向不存在 `content_item` 的 `user_content_push` 坏样本，这会污染失败重试与回归结果。

当前已做两层收口：

1. 代码层：
   - 入队前会先把“内容已不存在”的待推送记录终态化
   - 这些坏样本不会再继续进入 RabbitMQ 正常消费链路
2. 数据层：
   - 提供了一次性清理脚本，统一逻辑删除历史坏样本和对应告警

执行清理：

```bash
mysql -uroot -p112233665qw pingyu_info_diet < sql/cleanup_invalid_user_content_push.sql
```

这个脚本只会逻辑删除：

- `user_content_push` 中指向不存在内容的记录
- 对应 `alert_record`

不会删除正常 `content_item`，也不会改真实用户数据。

## 9. 测试

定向测试示例：

```bash
./mvnw -Dtest=UserContentPushServiceTest test
./mvnw -Dtest=PushQueueServiceTest test
```

全量测试：

```bash
./mvnw test
```

## 10. 当前已验证能力

截至 `2026-05-19`，本地已真实验证通过：

- 前后端本地启动
- 登录与角色跳转
- 用户推送配置闭环
- 日报重复触发幂等性
- RabbitMQ 异步消费链路
- 真实飞书日报 IM 推送
- 真实飞书 Base 内容推送

更完整的过程记录见：[PROJECT_PROGRESS.md](./PROJECT_PROGRESS.md)
