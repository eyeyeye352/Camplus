package com.camplus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DynamicDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceConfig.class);

    private HikariDataSource dataSource;

    @Bean
    public DataSource dataSource(DatabaseConfigService configService) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setJdbcUrl(configService.getJdbcUrl());
            hikariConfig.setUsername(configService.getCurrentUsername());
            hikariConfig.setPassword(configService.getCurrentPassword());
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(5);
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setConnectionTimeout(20000);
            hikariConfig.setInitializationFailTimeout(0);
            dataSource = new HikariDataSource(hikariConfig);
            log.info("[数据库] 默认连接成功");
        } catch (Exception e) {
            log.warn("[数据库] 默认连接失败，需要用户配置: {}", e.getMessage());
            dataSource = null;
        }
        return new DelegatingDataSource(configService, this);
    }

    public void refreshDataSource(DatabaseConfigService configService) {
        if (dataSource != null) {
            dataSource.close();
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(configService.getJdbcUrl());
        hikariConfig.setUsername(configService.getCurrentUsername());
        hikariConfig.setPassword(configService.getCurrentPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(20000);
        dataSource = new HikariDataSource(hikariConfig);
        log.info("[数据库] 数据源刷新成功");
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}