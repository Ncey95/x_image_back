package com.ncey95.x_image_back;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/x_image", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("Ncey95") // 设置作者
                            .outputDir(System.getProperty("user.dir") + "/src/main/resources/generator"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("") // 设置父包名 可为空
                            .mapper("mapper")
                            .entity("po")
                            .controller("controller")
                            .service("service")
                            .serviceImpl("service.impl")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/generator/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("space_user") // 设置需要生成的表名 多个表名用逗号隔开
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation() // 启用字段注解
                            .controllerBuilder()
                            .enableRestStyle(); // 启用 REST 风格
                             // 设置过滤表前缀
                })
                .execute();
    }
}