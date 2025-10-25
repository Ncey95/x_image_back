package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {


    private String searchText;

    private String namePrefix;

    private Integer count = 10;
}