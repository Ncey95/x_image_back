package com.ncey95.x_image_back.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.ncey95.x_image_back.config.CosClientConfig;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.manager.CosManager;
import com.ncey95.x_image_back.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    // inputSource输入源
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {

        // 1.校验图片
        validPicture(inputSource);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        // 3. 上传文件名 包含日期 随机字符串 后缀 uuid生成的是随机字符串 用于防止文件名冲突 例如 20251021_123456.jpg
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 3.上传文件 创建临时文件 获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);
            // 4.上传文件到 cos 存储桶
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片信息对象 返回构造好的图片上传结果对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 获取到图片处理结果
            // 处理图片压缩结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();

            // 构造一个 list集合 存储压缩后的图片结果
            List<CIObject> objectList = processResults.getObjectList();
            // 如果 压缩后的结果 不为空 则返回压缩后的结果
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0); // 压缩后的图片
                CIObject thumbnailCiObject = compressedCiObject; // 缩略图默认是压缩后的图片
                // 有生成缩略图 才生成
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1); // 缩略图结果
                }

                // 返回压缩后的图片结果
                return buildResult(originFilename, compressedCiObject,thumbnailCiObject);
            }

            return buildResult(originFilename, file, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6.删除临时文件 释放资源
            deleteTempFile(file);
        }
    }

    // 校验输入源 本地文件或者URL文件
    protected abstract void validPicture(Object inputSource);

    // 获取原始文件名
    protected abstract String getOriginFilename(Object inputSource);

    // 处理文件来源 本地文件或者URL文件 到临时文件
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    // 封装返回结果
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }

    // 封装返回结果 压缩后的图片结果 compressedCiObject是压缩后的图片信息对象 thumbnailCiObject是缩略图信息对象
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject, CIObject thumbnailCiObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult(); // 压缩后的图片上传结果对象
        int picWidth = compressedCiObject.getWidth(); // 压缩后的图片宽度
        int picHeight = compressedCiObject.getHeight(); // 压缩后的图片高度
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue(); // 压缩后的图片缩放比例
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());

        // 压缩后的图片地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey()); // 压缩后的图片上传地址

        // 缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    // 删除临时文件 释放资源
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
