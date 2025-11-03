package com.ncey95.x_image_back.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserVO implements Serializable {


    private Long id;


    private String userAccount;


    private String userName;


    private String userAvatar;


    private String userProfile;


    private String userRole;


    private Date createTime;

    private List<String> permissionList = new ArrayList<>();

    private static final long serialVersionUID = 1L;
}
