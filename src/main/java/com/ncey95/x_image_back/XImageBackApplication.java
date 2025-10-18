package com.ncey95.x_image_back;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.ncey95.x_image_back.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)// 开启AOP代理，暴露代理对象，用于在切面中获取代理对象
public class XImageBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(XImageBackApplication.class, args);
    }

}
