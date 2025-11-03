package com.ncey95.x_image_back.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {


    private String spaceName;


    private Integer spaceLevel;

    private Integer spaceType; // 空间类型：0-私有空间 1-团队空间

    private static final long serialVersionUID = 1L;
}
