package com.Mike12138210.SmartNote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 启动类，SpringBoot项目的入口
@MapperScan("com.Mike12138210.SmartNote.mapper")
@SpringBootApplication
public class SmartNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartNoteApplication.class, args);
    }
}