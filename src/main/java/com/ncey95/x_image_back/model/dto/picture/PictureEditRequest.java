package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 介绍
     */
    private String introduction;


    private String category;


    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
