package com.camplus.contribution.dao;

import com.camplus.contribution.pojo.UserContribution;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContributionDao {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/camplus_db"
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Connection getConnection() throws SQLException {
        String url = value("CAMPLUS_DB_URL", "camplus.db.url", DEFAULT_URL);
        String user = value("CAMPLUS_DB_USER", "camplus.db.user", "root");
        String password = value("CAMPLUS_DB_PASSWORD", "camplus.db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    public int insert(UserContribution contribution) throws SQLException {
        String sql = """
                INSERT INTO user_contributions (
                    user_id, contribution_type, title, content, status, create_time, update_time
                ) VALUES (?, ?, ?, ?, 0, NOW(), NOW())
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, contribution.getUserId());
            statement.setInt(2, contribution.getContributionType());
            statement.setString(3, contribution.getTitle());
            statement.setString(4, contribution.getContent());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<UserContribution> findByUserId(Integer userId, Integer status, int offset, int size)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM user_contributions
                WHERE user_id = ?
                """);
        if (status != null) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, userId);
            if (status != null) {
                statement.setInt(index++, status);
            }
            statement.setInt(index++, size);
            statement.setInt(index, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<UserContribution> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(map(resultSet));
                }
                return result;
            }
        }
    }

    public UserContribution findByIdAndUserId(Integer contributionId, Integer userId) throws SQLException {
        String sql = "SELECT * FROM user_contributions WHERE contribution_id = ? AND user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contributionId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean updatePending(UserContribution contribution) throws SQLException {
        String sql = """
                UPDATE user_contributions
                SET title = ?, content = ?, update_time = NOW()
                WHERE contribution_id = ? AND user_id = ? AND status = 0
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, contribution.getTitle());
            statement.setString(2, contribution.getContent());
            statement.setInt(3, contribution.getContributionId());
            statement.setInt(4, contribution.getUserId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deletePending(Integer contributionId, Integer userId) throws SQLException {
        String sql = "DELETE FROM user_contributions WHERE contribution_id = ? AND user_id = ? AND status = 0";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contributionId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private UserContribution map(ResultSet resultSet) throws SQLException {
        UserContribution contribution = new UserContribution();
        contribution.setContributionId(resultSet.getInt("contribution_id"));
        contribution.setUserId(resultSet.getInt("user_id"));
        contribution.setContributionType(resultSet.getInt("contribution_type"));
        contribution.setTitle(resultSet.getString("title"));
        contribution.setContent(resultSet.getString("content"));
        contribution.setStatus(resultSet.getInt("status"));
        contribution.setReviewComment(resultSet.getString("review_comment"));
        contribution.setCreateTime(nullableDateTime(resultSet, "create_time"));
        contribution.setUpdateTime(nullableDateTime(resultSet, "update_time"));
        return contribution;
    }

    private static LocalDateTime nullableDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private static String value(String envName, String propertyName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return System.getProperty(propertyName, defaultValue);
    }
}
