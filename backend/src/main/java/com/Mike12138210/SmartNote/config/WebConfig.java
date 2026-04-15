package com.Mike12138210.SmartNote.config;

import com.Mike12138210.SmartNote.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")                // 拦截所有 /api/ 开头的请求
                .excludePathPatterns("/api/auth/**", "/api/notes/public/**"); // 放行登录注册和公开笔记
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 映射 /uploads/** 到项目根目录下的uploads文件夹
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}