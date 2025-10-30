public static String getImagePageUrl(String imageUrl) {
    // 构建表单数据
    Map<String, Object> formData = new HashMap<>();
    formData.put("image", imageUrl);
    formData.put("tn", "pc");
    formData.put("from", "pc");
    formData.put("image_source", "PC_UPLOAD_URL");
    // 获取当前时间
    long uptime = System.currentTimeMillis();
    // 构建请求URL
    String url = "https://graph.baidu.com/upload?uptime=" + uptime;
    // 发送POST请求
    try {
        // hutool工具类发送POST请求 - 添加请求头信息
        HttpResponse response = HttpRequest.post(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form(formData)
                .timeout(10000)  // 增加超时时间
                .execute();
        
        // 检查响应状态码是否为200
        if (HttpStatus.HTTP_OK != response.getStatus()) {
            log.error("接口调用失败，状态码: {}", response.getStatus());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败，状态码: " + response.getStatus());
        }
        
        // 解析响应体为JSON
        String responseBody = response.body();
        log.info("接口响应: {}", responseBody); // 记录响应内容以便调试
        
        if (!JSONUtil.isJson(responseBody)) {
            log.error("响应不是有效的JSON格式: {}", responseBody);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "响应格式错误");
        }
        
        Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

        // 检查响应体是否包含有效数据
        if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
            log.error("接口返回无效状态: {}", result);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败，状态异常");
        }
        
        if (!result.containsKey("data") || !(result.get("data") instanceof Map)) {
            log.error("接口返回数据格式错误: {}", result);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "返回数据格式错误");
        }
        
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (!data.containsKey("url") || !(data.get("url") instanceof String)) {
            log.error("接口返回URL格式错误: {}", data);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效URL");
        }
        
        String rawUrl = (String) data.get("url");
        // 解码URL
        String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
        // 检查解码后的URL是否有效
        if (searchResultUrl == null || !searchResultUrl.startsWith("http")) {
            log.error("解码后URL无效: {}", searchResultUrl);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
        }
        return searchResultUrl;
    } catch (Exception e) {
        log.error("搜索失败", e);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败: " + e.getMessage());
    }
}