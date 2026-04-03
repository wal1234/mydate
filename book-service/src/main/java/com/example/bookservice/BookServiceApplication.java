package com.example.bookservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 图书微服务启动类。
 * <ul>
 *   <li>启用 Spring Boot 自动配置与组件扫描</li>
 *   <li>启用 Nacos 服务发现与注册</li>
 *   <li>扫描 MyBatis Mapper 接口</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.bookservice.mapper")
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }
}
