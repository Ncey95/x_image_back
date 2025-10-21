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
    private Long id;


    private String url;


    private String name;


    private String introduction;


    private String category;


    private String tags;


    private Long picSize;


    private Integer picWidth;


    private Integer picHeight;


    private Double picScale;


    private String picFormat;


    private Long userId;


    private Date createTime;


    private Date editTime;


    private Date updateTime;


    @TableLogic // 逻辑删除
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
