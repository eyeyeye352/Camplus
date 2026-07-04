package com.camplus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DynamicDataSourceConfig {

    @Bean
    public DataSource dataSource(DatabaseConfigService configService) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(configService.getJdbcUrl());
        hikariConfig.setUsername(configService.getCurrentUsername());
        hikariConfig.setPassword(configService.getCurrentPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(20000);
        return new HikariDataSource(hikariConfig);
    }
}