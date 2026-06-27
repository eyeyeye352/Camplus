package com.camplus.problem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // 数据库连接配置
    private static final String URL = "jdbc:mysql://localhost:3306/camplus_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // 保持 root 不变
    private static final String PASSWORD = "wrb3292958"; // 这里改成你的新密码

    static {
        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        // 这里会自动使用上面修改好的密码去尝试连接
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}