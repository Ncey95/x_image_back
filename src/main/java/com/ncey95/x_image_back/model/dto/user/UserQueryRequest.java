package com.ncey95.x_image_back.model.dto.user;

import com.ncey95.x_image_back.model.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true) // 调用父类的equals和hashCode方法
@Data
public class UserQueryRequest extends PageRequest implements Serializable {


    private Long id;


    private String userName;


    private String userAccount;


    private String userProfile;


    private String userRole;

    private static final long serialVersionUID = 1L;
}
