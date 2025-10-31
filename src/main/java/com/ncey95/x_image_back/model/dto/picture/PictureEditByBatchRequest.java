package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditByBatchRequest implements Serializable {


    private List<Long> pictureIdList;


    private Long spaceId;

    private String nameRule;

    private String category;


    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
