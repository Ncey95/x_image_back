package com.ncey95.x_image_back.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ncey95.x_image_back.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.ncey95.x_image_back.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ncey95.x_image_back.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云AI服务API接口类
 * 提供与阿里云AI服务交互的核心功能，包括创建扩图任务和查询扩图任务结果
 *
 * @author ncey95
 */
@Slf4j
@Component
public class AliYunAiApi {

    /**
     * 阿里云AI服务的API密钥
     * 从Spring配置文件中注入，用于身份验证
     */
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    /**
     * 创建扩图任务的API接口URL
     * 使用阿里云DashScope服务的图像扩图功能
     */
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    /**
     * 查询扩图任务结果的API接口URL模板
     * 需要传入任务ID进行格式化
     */
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建扩图任务
     * 向阿里云AI服务发送扩图请求，返回任务创建结果
     *
     * @param createOutPaintingTaskRequest 扩图任务请求参数，包含待处理图像和扩图配置
     * @return 扩图任务创建响应，包含任务ID等信息
     * @throws BusinessException 当请求参数为空、API调用失败或响应异常时抛出
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        // 参数校验
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }

        // 构建HTTP请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey) // 设置认证头
                .header("X-DashScope-Async", "enable") // 启用异步处理
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue()) // 设置内容类型
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest)); // 请求体JSON序列化

        try (HttpResponse httpResponse = httpRequest.execute()) { // 执行请求并自动关闭资源
            // 检查HTTP响应状态
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }

            // 解析响应结果
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            String errorCode = response.getCode();

            // 检查业务错误码
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
            }

            return response;
        } catch (Exception e) {
            // 捕获并记录异常
            log.error("创建扩图任务异常", e);
            throw e instanceof BusinessException ? (BusinessException) e : new BusinessException(ErrorCode.OPERATION_ERROR, "创建扩图任务失败");
        }
    }

    /**
     * 查询扩图任务结果
     * 根据任务ID获取阿里云AI服务扩图任务的处理状态和结果
     *
     * @param taskId 扩图任务的唯一标识符
     * @return 扩图任务查询响应，包含任务状态和处理结果
     * @throws BusinessException 当任务ID为空或API调用失败时抛出
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        // 参数校验
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }

        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey) // 设置认证头
                .execute()) { // 执行请求并自动关闭资源

            // 检查HTTP响应状态
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }

            // 解析响应结果
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        } catch (Exception e) {
            // 捕获并记录异常
            log.error("获取扩图任务结果异常，taskId:{}", taskId, e);
            throw e instanceof BusinessException ? (BusinessException) e : new BusinessException(ErrorCode.OPERATION_ERROR, "获取扩图任务结果失败");
        }
    }
}

