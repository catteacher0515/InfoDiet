package com.pingyu.infodiet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.pingyu.infodiet.mapper")
public class InfoDietApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfoDietApplication.class, args);
    }
}
