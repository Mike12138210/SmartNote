package com.Mike12138210.SmartNote.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MyBatis-Plus 的拦截器容器（可以添加多个拦截器，比如分页、乐观锁等）
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 创建分页拦截器（PaginationInnerInterceptor），并指定数据库类型为 MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 设置最大单页限制（防止恶意请求过大页码），这里设为 500 条
        paginationInterceptor.setMaxLimit(500L);

        // 将分页拦截器添加到容器中
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
