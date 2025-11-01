package com.ncey95.x_image_back.model.vo.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用情况分析响应对象
 * 用于返回指定空间（或整体）的存储使用统计信息，包括存储空间和图片数量的使用情况
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 已使用的存储空间大小，单位：字节
     */
    private Long usedSize;

    /**
     * 最大允许使用的存储空间大小，单位：字节
     */
    private Long maxSize;

    /**
     * 存储空间使用率，计算公式：usedSize / maxSize，范围0-1
     */
    private Double sizeUsageRatio;

    /**
     * 已使用的图片数量
     */
    private Long usedCount;

    /**
     * 最大允许存储的图片数量
     */
    private Long maxCount;

    /**
     * 图片数量使用率，计算公式：usedCount / maxCount，范围0-1
     */
    private Double countUsageRatio;

    /**
     * 序列化版本ID，用于Java对象序列化和反序列化
     */
    private static final long serialVersionUID = 1L;
}
