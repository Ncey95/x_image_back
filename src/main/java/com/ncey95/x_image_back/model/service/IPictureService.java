package com.ncey95.x_image_back.model.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ncey95.x_image_back.model.dto.picture.PictureQueryRequest;
import com.ncey95.x_image_back.model.dto.picture.PictureUploadRequest;
import com.ncey95.x_image_back.model.po.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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

    // 构造查询条件
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    // 获取图片VO 包含用户信息
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    //分页获取图片封装：
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    //编写图片数据校验方法，用于更新和修改图片时进行判断：
    void validPicture(Picture picture);
}
