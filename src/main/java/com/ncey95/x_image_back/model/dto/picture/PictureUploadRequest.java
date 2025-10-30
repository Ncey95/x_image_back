package com.ncey95.x_image_back.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    // 继承Serializable 是为了在网络传输时序列化和反序列化
    private Long id;// 图片id

    private String fileUrl; // 图片url

    private String picName;// 图片名称前缀

    private Long spaceId;// 空间id

    private static final long serialVersionUID = 1L;// 序列化版本号
}
