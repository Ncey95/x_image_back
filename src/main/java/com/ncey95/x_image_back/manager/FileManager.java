package com.ncey95.x_image_back.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.ncey95.x_image_back.config.CosClientConfig;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import com.ncey95.x_image_back.model.dto.file.UploadPictureResult;

import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // 上传图片到 cos 存储桶并解析图片信息
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {

        // 校验图片是否合法
        validPicture(multipartFile);

        // 生成随机字符串作为文件名
        String uuid = RandomUtil.randomString(16);
        // 获取原始文件名
        String originFilename = multipartFile.getOriginalFilename();
        // 构造上传文件名 时间_随机字符串.后缀
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        // 构造上传路径 /{uploadPathPrefix}/{uploadFilename}
        // uploadPathPrefix是用户指定的前缀,uploadFilename是构造的文件名
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 上传文件到临时文件
            multipartFile.transferTo(file);
            // 上传文件到 cos 存储桶 拿到上传结果对象
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 从上传结果对象中获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            // 计算图片缩放比 保留两位小数
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            // 从原始文件名中提取主文件名 不包含后缀
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            // 设置图片宽度
            uploadPictureResult.setPicWidth(picWidth);
            // 设置图片高度
            uploadPictureResult.setPicHeight(picHeight);
            // 设置图片缩放比
            uploadPictureResult.setPicScale(picScale);
            // 设置图片格式
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            // 设置图片大小
            uploadPictureResult.setPicSize(FileUtil.size(file));
            //url 是 cos 存储桶中的对象地址 拼接 cos 存储桶的域名 得到完整的图片 url
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 上传完成后删除临时文件
            this.deleteTempFile(file);
        }
    }

    // 校验图片是否合法
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 5 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");

        // hutool 工具类获取文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        // 允许上传的文件类型列表
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    // 删除临时文件
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
