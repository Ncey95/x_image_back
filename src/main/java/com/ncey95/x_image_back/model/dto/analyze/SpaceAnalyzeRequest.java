package com.ncey95.x_image_back.model.dto.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间分析请求对象
 * 用于指定空间分析的查询范围，支持查询特定空间、公开空间或全部空间的数据
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 空间ID
     * 当查询特定空间时使用，若为null则表示不按空间ID查询
     */
    private Long spaceId;

    /**
     * 是否查询公开空间
     * 设置为true时表示仅查询无所属空间(spaceId为null)的公开图片
     */
    private boolean queryPublic;

    /**
     * 是否查询全部空间
     * 设置为true时表示查询系统中所有空间的图片数据
     */
    private boolean queryAll;

    /**
     * 序列化版本ID，用于Java对象序列化和反序列化
     */
    private static final long serialVersionUID = 1L;
}
