package com.Mike12138210.SmartNote;

import com.Mike12138210.SmartNote.service.impl.AiService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

// 启动类，SpringBoot项目的入口
@MapperScan("com.Mike12138210.SmartNote.mapper")
@SpringBootApplication
@EnableScheduling
public class SmartNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartNoteApplication.class, args);
    }
/*
    @Bean
    public CommandLineRunner testAi(AiService aiService){
        return args -> {
            String testContent = "这是一篇关于Spring Boot的笔记。今天学习了MyBatis-Plus的分页查询和逻辑删除。";
            Map<String, Object> result = aiService.generateSummaryAndKeyPoints(testContent);
            System.out.println("摘要：" + result.get("summary"));
            System.out.println("要点：" + result.get("keyPoints"));
        };
    }*/
}