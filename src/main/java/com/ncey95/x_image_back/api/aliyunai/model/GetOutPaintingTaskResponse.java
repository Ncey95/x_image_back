package com.ncey95.x_image_back.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阿里云AI扩图任务查询响应模型
 * 用于接收和解析阿里云AI服务返回的扩图任务结果信息
 *
 * @author ncey95
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识，用于问题排查和日志追踪
     */
    private String requestId;

    /**
     * 扩图任务输出结果对象
     * 包含任务状态、处理结果和相关信息
     */
    private Output output;

    /**
     * 扩图任务输出结果内部类
     * 封装扩图任务的详细处理结果和状态信息
     */
    @Data
    public static class Output {

        /**
         * 任务唯一标识符
         */
        private String taskId;

        /**
         * 任务状态
         * 常见状态：RUNNING(运行中)、SUCCEEDED(成功)、FAILED(失败)等
         */
        private String taskStatus;

        /**
         * 任务提交时间
         * 格式通常为ISO 8601日期时间格式
         */
        private String submitTime;

        /**
         * 任务调度时间
         * 任务开始执行的时间
         */
        private String scheduledTime;

        /**
         * 任务结束时间
         * 任务完成(成功或失败)的时间
         */
        private String endTime;

        /**
         * 输出图像的URL地址
         * 扩图处理完成后的结果图像访问链接
         */
        private String outputImageUrl;

        /**
         * 任务处理结果状态码
         * 成功时通常为"Success"或具体数字，失败时包含错误码
         */
        private String code;

        /**
         * 任务处理结果消息
         * 包含处理结果说明或错误详情
         */
        private String message;

        /**
         * 任务度量指标
         * 包含任务处理的统计信息
         */
        private TaskMetrics taskMetrics;
    }

    /**
     * 任务度量指标内部类
     * 封装任务处理的统计数据
     */
    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         * 通常为1，表示单个扩图任务
         */
        private Integer total;

        /**
         * 成功任务数
         */
        private Integer succeeded;

        /**
         * 失败任务数
         */
        private Integer failed;
    }
}