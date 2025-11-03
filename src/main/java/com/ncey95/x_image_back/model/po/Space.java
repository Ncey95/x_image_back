package com.ncey95.x_image_back.model.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 空间
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-28
 */
@Data
@TableName("space")
public class Space implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    @TableField("spaceName")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @TableField("spaceLevel")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @TableField("maxSize")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @TableField("maxCount")
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @TableField("totalSize")
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @TableField("totalCount")
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @TableField("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @TableField("editTime")
    private Date editTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Byte isDelete;

     /**
     * 空间类型：0-私有空间 1-团队空间
     */
    @TableField("spaceType")
    private Integer spaceType;
}
