package com.camplus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan({
        "com.camplus.login.mappers",
        "com.camplus.admin.Mappers",
        "com.camplus.faq.mappers",
        "com.camplus.contribution.mappers",
        "com.camplus.vector.mappers"
})
@SpringBootApplication
public class CamplusApplication {
    public static void main(String[] args) {
        SpringApplication.run(CamplusApplication.class, args);
    }
}
