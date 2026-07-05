-- 系统必需数据（建表后执行）

USE camplus_db;

-- 初始化管理员账户（密码: 123456 的 MD5）
INSERT INTO users (username, password_hash, email, role)
VALUES ('Administrator', 'e10adc3949ba59abbe56e057f20f883e', 'admin@camplus.com', 1)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    email = VALUES(email),
    role = VALUES(role);
