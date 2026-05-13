-- 演示账号初始化脚本
-- 管理员账号：demo_admin / Infodiet123!
-- 普通用户账号：demo_user / Infodiet123!

INSERT INTO user_profile (
    nickname,
    username,
    password,
    role,
    pushChannel,
    dailyPushLimit,
    pushCooldownHours,
    status,
    isDelete
) VALUES
(
    '演示管理员',
    'demo_admin',
    '$2a$10$wtLiUGKlGQUfXFttnc0y7OthcMzyJjFOuAkxDR7QkG8954wGDEvGy',
    'admin',
    'feishu',
    10,
    0,
    1,
    0
),
(
    '演示用户',
    'demo_user',
    '$2a$10$wtLiUGKlGQUfXFttnc0y7OthcMzyJjFOuAkxDR7QkG8954wGDEvGy',
    'user',
    'feishu',
    10,
    0,
    1,
    0
)
ON DUPLICATE KEY UPDATE
nickname = VALUES(nickname),
password = VALUES(password),
role = VALUES(role),
pushChannel = VALUES(pushChannel),
dailyPushLimit = VALUES(dailyPushLimit),
pushCooldownHours = VALUES(pushCooldownHours),
status = VALUES(status),
isDelete = VALUES(isDelete);
