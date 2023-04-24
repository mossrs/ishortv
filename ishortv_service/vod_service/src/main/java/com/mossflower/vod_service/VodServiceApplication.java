package com.mossflower.vod_service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author z's'b
 */
@EnableScheduling
@SpringBootApplication
@MapperScan({"com.gitee.sunchenbin.mybatis.actable.dao.*", "com.mossflower.vod_service.mapper"})
@ComponentScan(basePackages = {"com.gitee.sunchenbin.mybatis.actable.manager.*", "com.mossflower"})
public class VodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VodServiceApplication.class, args);
    }

}
