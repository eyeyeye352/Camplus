package com.camplus.config;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DatabaseConfigService {
    //数据库连接默认值，方便测试
    private static final String DEFAULT_USERNAME = " ";
    private static final String DEFAULT_PASSWORD = " ";
    private static final String URL_TEMPLATE = "jdbc:mysql://localhost:3306/camplus_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";

    // 优先从系统属性读取（用于 --import-only 模式传入凭据），否则使用默认值
    private volatile String currentUsername = System.getProperty("camplus.db.user", DEFAULT_USERNAME);
    private volatile String currentPassword = System.getProperty("camplus.db.pass", DEFAULT_PASSWORD);
    private final AtomicBoolean connectionValid = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        validateConnection();
    }

    public boolean validateConnection() {
        return validateConnection(currentUsername, currentPassword);
    }

    public boolean validateConnection(String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL_TEMPLATE, username, password)) {
            connectionValid.set(true);
            return true;
        } catch (SQLException e) {
            connectionValid.set(false);
            return false;
        }
    }

    public boolean updateConnection(String username, String password) {
        if (validateConnection(username, password)) {
            this.currentUsername = username;
            this.currentPassword = password;
            return true;
        }
        return false;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public boolean isConnectionValid() {
        return connectionValid.get();
    }

    public String getJdbcUrl() {
        return URL_TEMPLATE;
    }
}