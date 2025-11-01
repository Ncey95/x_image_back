package com.ncey95.x_image_back.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阿里云AI扩图任务创建响应模型
 * 用于封装阿里云AI图像扩图API创建任务后的响应数据结构
 */
@Data // Lombok注解，自动生成getter、setter、toString等方法
@NoArgsConstructor // Lombok注解，自动生成无参构造函数
@AllArgsConstructor // Lombok注解，自动生成全参构造函数
public class CreateOutPaintingTaskResponse {
    
    /**
     * 任务输出信息对象
     * 包含任务ID和任务状态等核心信息
     */
    private Output output;
    
    
    /**
     * 输出信息内部类
     * 封装扩图任务的具体执行结果信息
     */
    @Data // Lombok注解，自动生成getter、setter、toString等方法
    public static class Output {
        
        /**
         * 扩图任务唯一标识符
         * 用于后续查询任务状态和获取结果
         */
        private String taskId;
        
        /**
         * 任务当前状态
         * 可能的值包括：PENDING（待处理）、RUNNING（运行中）、SUCCEEDED（成功）、FAILED（失败）等
         */
        private String taskStatus;
    }
    
    /**
     * 响应状态码
     * 表示API调用是否成功，成功时通常为"200"或"0"
     */
    private String code;
    
    /**
     * 响应消息
     * 描述API调用结果的详细信息，失败时包含错误描述
     */
    private String message;
    
    /**
     * 请求唯一标识符
     * 用于定位和跟踪API请求，通常用于问题排查
     */
    private String requestId;
}