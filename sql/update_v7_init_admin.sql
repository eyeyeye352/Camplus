USE camplus_db;

INSERT INTO users (username, password_hash, email, phone, nickname, avatar_url, role, status, login_error_count)
VALUES ('Administrator', 'e10adc3949ba59abbe56e057f20f883e', 'admin@camplus.com', '13800138000', '管理员', NULL, 1, 1, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    email = VALUES(email),
    phone = VALUES(phone),
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status);