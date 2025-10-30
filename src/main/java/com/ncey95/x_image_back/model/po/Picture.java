package com.ncey95.x_image_back.model.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 图片
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-21
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;//id

    private String url; //url地址

    private String thumbnailUrl; // 缩略图url

    private String picColor; // 图片主色调


     // 空间id
    private Long spaceId;

    private String name; //图片名字

    // 图片的介绍
    private String introduction;
    // 图片的分类
    private String category;
    // 标签
    private String tags;
    // 大小
    private Long picSize;
    // 宽度
    private Integer picWidth;
    // 高度
    private Integer picHeight;
    // 缩放
    private Double picScale;
    // 格式
    private String picFormat;
    // 图片的上传用户id
    private Long userId;
    // 上传时间
    private Date createTime;
    // 编辑时间
    private Date editTime;
    // 更新时间
    private Date updateTime;
    // 审核状态：0-待审核; 1-通过; 2-拒绝
    private Integer reviewStatus;
    // 审核信息
    private String reviewMessage;

    // 审核人ID
    private Long reviewerId;
    //审核时间
    private LocalDateTime reviewTime;

    @TableLogic // 逻辑删除
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
