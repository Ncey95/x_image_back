package com.ncey95.x_image_back.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 阿里云AI扩图任务请求模型
 * 用于构建向阿里云AI服务发送扩图任务的请求参数
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    /**
     * AI模型名称，固定为"image-out-painting"（图像扩图）
     */
    private String model = "image-out-painting";

    /**
     * 输入参数对象，包含待处理的图像信息
     */
    private Input input;

    /**
     * 扩图任务参数配置，包含各种控制扩图行为的参数
     */
    private Parameters parameters;

    /**
     * 输入参数内部类
     * 封装待处理图像的相关信息
     */
    @Data
    public static class Input {

        /**
         * 待处理图像的URL地址
         * @Alias注解用于JSON序列化/反序列化时的字段映射（下划线命名法）
         */
        @Alias("image_url")
        private String imageUrl;
    }

    /**
     * 扩图参数配置内部类
     * 包含控制扩图行为的各种详细参数
     */
    @Data
    public static class Parameters implements Serializable {

        /**
         * 图像旋转角度，用于调整图像方向
         */
        private Integer angle;

        /**
         * 输出图像的宽高比
         * @Alias注解用于JSON序列化/反序列化时的字段映射
         */
        @Alias("output_ratio")
        private String outputRatio;

        /**
         * X轴方向的缩放比例
         * @Alias用于下划线命名法映射
         * @JsonProperty用于指定JSON属性名称（驼峰命名法）
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * Y轴方向的缩放比例
         * @Alias用于下划线命名法映射
         * @JsonProperty用于指定JSON属性名称（驼峰命名法）
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 顶部扩展的像素值
         * @Alias用于下划线命名法映射
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 底部扩展的像素值
         * @Alias用于下划线命名法映射
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 左侧扩展的像素值
         * @Alias用于下划线命名法映射
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 右侧扩展的像素值
         * @Alias用于下划线命名法映射
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 是否启用最高质量模式
         * @Alias用于下划线命名法映射
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 是否限制图像大小
         * @Alias用于下划线命名法映射
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 是否添加水印，默认为false（不添加）
         * @Alias用于下划线命名法映射
         */
        @Alias("add_watermark")
        private Boolean addWatermark = false;
    }
}