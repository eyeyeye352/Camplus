package com.camplus.problem;

import com.camplus.problem.controller.FaqProblemServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<FaqProblemServlet> faqServletRegistration() {
        // 将你的 Servlet 注册到 /faq 路径下
        return new ServletRegistrationBean<>(new FaqProblemServlet(), "/faq");
    }
}