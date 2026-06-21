package com.camplus.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;


@SpringBootApplication(scanBasePackages = {"com.camplus"})
@MapperScan("com.camplus.vector.mappers") // 🌟 新增：告诉 MyBatis 去哪里找数据库 Mapper
public class CampusQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusQaApplication.class, args);
    }

}