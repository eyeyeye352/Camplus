package com.camplus;

import com.camplus.config.DatabaseConfigService;
import com.camplus.config.DynamicDataSourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DatabaseConfigService databaseConfigService;
    private final DynamicDataSourceConfig dataSourceConfig;

    public WebConfig(DatabaseConfigService databaseConfigService, DynamicDataSourceConfig dataSourceConfig) {
        this.databaseConfigService = databaseConfigService;
        this.dataSourceConfig = dataSourceConfig;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/home/index.html");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("js", MediaType.parseMediaType("application/javascript"));
        configurer.mediaType("mjs", MediaType.parseMediaType("application/javascript"));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DbConfigInterceptor(databaseConfigService, dataSourceConfig))
                .addPathPatterns("/**")
                .excludePathPatterns("/db-config.html", "/api/db/**", "/static/**", "/error");
    }

    private static class DbConfigInterceptor implements HandlerInterceptor {
        private final DatabaseConfigService databaseConfigService;
        private final DynamicDataSourceConfig dataSourceConfig;

        public DbConfigInterceptor(DatabaseConfigService databaseConfigService, DynamicDataSourceConfig dataSourceConfig) {
            this.databaseConfigService = databaseConfigService;
            this.dataSourceConfig = dataSourceConfig;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (!databaseConfigService.isConnectionValid() || dataSourceConfig.getDataSource() == null) {
                String requestUri = request.getRequestURI();
                if (!requestUri.endsWith("/db-config.html") && !requestUri.startsWith("/api/db/")) {
                    response.sendRedirect("/db-config.html");
                    return false;
                }
            }
            return true;
        }
    }
}