package com.ncey95.x_image_back.model.vo;

import cn.hutool.json.JSONUtil;
import com.ncey95.x_image_back.model.po.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {


    private Long id;


    private String url;

    private String thumbnailUrl; // 缩略图url

    private String picColor; // 图片主色调


     // 空间id
    private Long spaceId;


    private String name;


    private String introduction;


    private List<String> tags;


    private String category;


    private Long picSize;


    private Integer picWidth;


    private Integer picHeight;


    private Double picScale;


    private String picFormat;


    private Long userId;


    private Date createTime;


    private Date editTime;


    private Date updateTime;


    private UserVO user;

     // 权限列表
    private List<String> permissionList;



    private static final long serialVersionUID = 1L;

    // 将PictureVO对象转换为Picture对象
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);

        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    // 将Picture对象转换为PictureVO对象
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);

        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
