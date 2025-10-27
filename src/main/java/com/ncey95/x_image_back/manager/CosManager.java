package com.ncey95.x_image_back.manager;

import cn.hutool.core.io.FileUtil;
import com.ncey95.x_image_back.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    //key是文件路径 file是文件 上传文件到 cos 存储桶
    public PutObjectResult putObject(String key, File file) { // 上传文件到 cos 存储桶
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key) { // 获取 cos 存储桶中的对象
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    //上传文件 并附带图片信息
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);// 上传文件到 cos 存储桶
        // 解析图片信息 也被视作图片处理
        PicOperations picOperations = new PicOperations();
        // 设置是否需要返回原图信息
        picOperations.setIsPicInfo(1);

        //图片压缩 规则列表
        List<PicOperations.Rule> rules=new ArrayList<>();

        // 1  图片压缩 转换为WEBP格式
        String webpkey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule(); // 构建一个压缩规则
        compressRule.setFileId(webpkey); // 设置压缩后的文件名
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp"); // 设置压缩规则为 webp
        rules.add(compressRule); // 添加压缩规则

        // 2 缩略图处理 仅对>20k的图片进行处理
        // 仅对大于k的图片进行压缩
        if (file.length() > 20 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule(); // 构建一个压缩规则
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);//  设置压缩后的文件名
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256)); // 设置压缩规则为 webp
            rules.add(thumbnailRule);
        }

        // 构造处理参数
        picOperations.setRules(rules); // 设置图片压缩规则
        putObjectRequest.setPicOperations(picOperations); // 设置图片处理参数
        return cosClient.putObject(putObjectRequest); // 上传文件到 cos 存储桶
    }

    //删除cos存储桶中的对象
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
