package com.ncey95.x_image_back.model.service;

import com.ncey95.x_image_back.model.dto.picture.PictureUploadRequest;
import com.ncey95.x_image_back.model.po.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 图片 服务类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-21
 */
public interface IPictureService extends IService<Picture> {

    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);
    //multipartFile是图片文件
    //pictureUploadRequest是图片上传请求参数
    //loginUser是登录用户
}
