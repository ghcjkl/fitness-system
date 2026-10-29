package com.fitness.fitness.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final AdminLogInterceptor adminLogInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor, AdminInterceptor adminInterceptor,
                     AdminLogInterceptor adminLogInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.adminLogInterceptor = adminLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 路径匹配不含 context-path，静态图片与登录注册无需登录
                .excludePathPatterns("/user/login", "/user/register", "/upload/**");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
        registry.addInterceptor(adminLogInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:/fitness_upload/");
    }
}
