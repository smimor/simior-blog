package org.simior;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * simior-blog 博客系统后端应用入口类
 * <p>
 * 基于 Spring Boot 4.1 + MyBatis-Plus + Sa-Token 构建的全栈博客系统服务端。
 * 启用事务管理和缓存支持。
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@MapperScan("org.simior.mapper")
public class BlogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogServerApplication.class, args);
    }

}
