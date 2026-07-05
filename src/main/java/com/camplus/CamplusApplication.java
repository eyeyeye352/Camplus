package com.camplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@MapperScan({
        "com.camplus.login.mappers",
        "com.camplus.admin.Mappers",
        "com.camplus.faq.mappers",
        "com.camplus.contribution.mappers",
        "com.camplus.vector.mappers"
})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
public class CamplusApplication {
    public static void main(String[] args) {
        boolean importOnly = Arrays.asList(args).contains("--import-only");

        // import-only 模式下，提前解析 DB 凭据并设为系统属性，让 DatabaseConfigService 在 Spring 初始化时就能读取
        if (importOnly) {
            for (int i = 0; i < args.length - 1; i++) {
                if ("--db-user".equals(args[i])) {
                    System.setProperty("camplus.db.user", args[i + 1]);
                }
                if ("--db-pass".equals(args[i])) {
                    System.setProperty("camplus.db.pass", args[i + 1]);
                }
            }
            // 激活 import-only profile，使用精简日志配置
            System.setProperty("spring.profiles.active", "import-only");
            new SpringApplicationBuilder(CamplusApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        } else {
            SpringApplication.run(CamplusApplication.class, args);
        }
    }
}