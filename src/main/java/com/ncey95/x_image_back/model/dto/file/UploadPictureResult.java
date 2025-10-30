package com.ncey95.x_image_back.model.dto.file;

import lombok.Data;

@Data
public class UploadPictureResult {


    private String url;

    private String thumbnailUrl; // 缩略图url

    private String picColor; // 图片主色调



    private String picName;


    private Long picSize;


    private int picWidth;


    private int picHeight;


    private Double picScale;


    private String picFormat;

}
