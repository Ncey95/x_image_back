package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByColorRequest implements Serializable {


    private String picColor;


    private Long spaceId;

    private static final long serialVersionUID = 1L;
}

