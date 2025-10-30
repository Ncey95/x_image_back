package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {


    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
