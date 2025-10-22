package com.ncey95.x_image_back.model.vo;

import com.ncey95.x_image_back.model.mapper.PictureMapper;
import lombok.Data;

import java.util.List;

//图片标签分类 列表视图
@Data
public class PictureTagCategory {
    private List<String> tagList;

    private List<String> categoryList;


}
