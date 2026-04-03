package com.example.scheduleservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 日程管理微服务启动类。
 * 包含每日日程登记与日常文档编辑管理，依赖 Nacos 注册、book-common、MyBatis-Plus（SQL 在 XML）。
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.scheduleservice.mapper")
public class ScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServiceApplication.class, args);
    }
}
