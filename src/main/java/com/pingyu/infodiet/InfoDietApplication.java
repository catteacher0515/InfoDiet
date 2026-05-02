package com.pingyu.infodiet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pingyu.infodiet.mapper")
public class InfoDietApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfoDietApplication.class, args);
    }
}
