package com.ncey95.x_image_back.model.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ncey95.x_image_back.model.dto.picture.*;
import com.ncey95.x_image_back.model.po.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.vo.PictureVO;
import io.swagger.models.auth.In;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 图片 服务类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-21
 */
public interface IPictureService extends IService<Picture> {

    // inputSource 输入源 可以是 MultipartFile 或 String的url 类型
    PictureVO uploadPicture(Object inputSource,
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

    //图片审核
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    //填充审核参数
    void fillReviewParams(Picture picture, User loginUser);

    // 批量上传图片通过url
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    // 异步清除图片文件
    @Async
    void clearPictureFile(Picture oldPicture);

    void checkPictureAuth(User loginUser, Picture picture);

    void deletePicture(long pictureId, User loginUser);

    // 更新图片
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    // 按颜色搜索图片
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);
}
