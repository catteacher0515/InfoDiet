# InfoDiet Frontend And Auth Workspace Design

**目标**

为 InfoDiet 增加一套可实际使用的前端工作台，并补齐基于 JWT 的登录认证闭环，使系统从“接口驱动的后端项目”升级为“用户可直接使用的产品化系统”。

**设计结论**

1. 前端采用单独的 `frontend/` 工程，放在仓库根目录，不与 Spring Boot 静态资源混写。
2. 前端采用单应用多区域路由结构，分为公共区、用户区、管理区、运维区。
3. 后端采用 `JWT + admin/user` 的最小认证模型。
4. 第一版直接扩展 `user_profile`，不单独拆 `user_auth`，优先保证开发速度与闭环可验证。

## 1. 前端信息架构

### 1.1 公共区

- `/login`
- `/register`

职责：

- 未登录用户完成注册与登录
- 登录成功后根据角色跳转默认区域

### 1.2 用户区 `/workspace`

- `/workspace/dashboard`
- `/workspace/subscriptions`
- `/workspace/content`
- `/workspace/pushes`

职责：

- 管理当前登录用户自己的订阅配置
- 查看与自己有关的内容、匹配结果与推送结果

### 1.3 管理区 `/admin`

- `/admin/dashboard`
- `/admin/users`
- `/admin/subscriptions`

职责：

- 用户管理
- 平台级概览
- 全局订阅配置查看

### 1.4 运维区 `/ops`

- `/ops/dashboard`
- `/ops/tasks`
- `/ops/push-failures`
- `/ops/alerts`

职责：

- 查看任务执行情况
- 手动触发任务
- 失败推送处理
- 告警查看与补发

## 2. 认证与权限设计

### 2.1 认证闭环

第一版实现：

- 注册
- 登录
- 获取当前用户信息
- 退出登录
- 管理员创建用户

第一版暂不实现：

- refresh token
- 多端登录管理
- 验证码
- 复杂 RBAC

### 2.2 角色模型

- `admin`
- `user`

权限边界：

- `user` 仅访问自己的用户区数据
- `admin` 可访问管理区与运维区

### 2.3 JWT 方案

- 登录成功后返回 JWT
- 前端通过 `Authorization: Bearer <token>` 发送
- 后端通过统一过滤器解析用户信息
- 服务层按当前登录用户完成数据隔离

## 3. 数据模型设计

第一版直接扩展 `user_profile`：

- `username`：登录账号
- `password`：加密后的密码
- `role`：角色

原因：

- 当前是单体项目
- 认证闭环简单
- 能最快支撑前端落地

后续如果拆微服务，再拆认证中心或 `user_auth` 表。

## 4. 页面与接口映射

### 4.1 新增认证接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `POST /api/auth/admin/create`

### 4.2 新增聚合接口

- `GET /api/workspace/dashboard/me`
- `GET /api/admin/dashboard`
- `GET /api/ops/dashboard`

### 4.3 新增用户管理接口

- `GET /api/admin/users`

### 4.4 逐步收口为当前用户接口

现有依赖 `userId` 的查询接口，后续逐步补为 `/me` 风格，避免前端登录后继续手动传用户 ID。

## 5. 实现顺序

1. 补后端认证字段与 SQL
2. 补 JWT 工具、过滤器、认证接口
3. 补管理端用户列表与三个 dashboard 聚合接口
4. 初始化 `frontend/` 工程
5. 先完成登录、注册、布局壳子、路由守卫
6. 再接用户区、管理区、运维区核心页面

## 6. 验证方式

后端验证：

- 单元测试
- 控制器测试
- 本地接口联调

前端验证：

- 本地启动
- 登录流程走通
- 角色跳转正确
- 核心页面能读到真实接口数据
