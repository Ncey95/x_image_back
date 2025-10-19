package com.ncey95.x_image_back.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 1694583362000L;

    private String userAccount; // 用户名
    private String password; // 密码
}
