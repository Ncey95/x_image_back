package com.ncey95.x_image_back.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    // 校验图片
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

    }

    // 获取原始文件名
    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;

        return FileUtil.mainName(fileUrl);
    }

    // 处理文件来源 URL文件 到临时文件
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时文件
        HttpUtil.downloadFile(fileUrl, file);
    }
}
