package com.ncey95.x_image_back.model.dto.picture;

import com.ncey95.x_image_back.model.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    // id
    private Long id;

    // 名字
    private String name;
    // 简介
    private String introduction;
    // 分类
    private String category;
    // 标签列
    private List<String> tags;
    // 图片大小
    private Long picSize;

    // 图片宽度
    private Integer picWidth;
    // 图片高度
    private Integer picHeight;

    // 图片缩放比例
    private Double picScale;

    // 图片格式
    private String picFormat;

    // 搜索文本
    private String searchText;

    // 用户id
    private Long userId;
    // 审核状态
    private Integer reviewStatus;

    // 审核信息
    private String reviewMessage;

    // 审核人id
    private Long reviewerId;

    // 空间id
    private Long spaceId;
    // 是否查询空间下的图片 为true时查询所有图片 为false时查询空间下的图片
    private boolean nullSpaceId;

    private Date startEditTime;

     private Date endEditTime;

    private static final long serialVersionUID = 1L;
}
