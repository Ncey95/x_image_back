package com.ncey95.x_image_back;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ncey95.x_image_back.mapper")
@SpringBootApplication
public class XImageBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(XImageBackApplication.class, args);
    }

}
