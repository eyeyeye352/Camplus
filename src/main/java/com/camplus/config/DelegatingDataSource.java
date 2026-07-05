package com.camplus.config;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DelegatingDataSource extends AbstractDataSource {

    private final DatabaseConfigService configService;
    private final DynamicDataSourceConfig config;

    public DelegatingDataSource(DatabaseConfigService configService, DynamicDataSourceConfig config) {
        this.configService = configService;
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        DataSource delegate = config.getDataSource();
        if (delegate == null) {
            throw new SQLException("数据库未配置，请先访问 /db-config.html 配置数据库连接");
        }
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }
}