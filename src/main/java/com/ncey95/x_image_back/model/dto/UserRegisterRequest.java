package com.ncey95.x_image_back.model.dto;

import lombok.Data;

import java.io.Serializable;

//用户注册 用于接受请求参数的类
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 1694583362000L;

    private String userAccount; // 用户名
    private String password; // 密码
    private String checkPassword; // 确认密码
}
