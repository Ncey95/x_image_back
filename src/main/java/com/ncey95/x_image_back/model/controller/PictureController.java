package com.ncey95.x_image_back.model.controller;

import com.ncey95.x_image_back.annotation.AuthCheck;
import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import com.ncey95.x_image_back.constant.UserConstant;
import com.ncey95.x_image_back.model.dto.picture.PictureUploadRequest;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.service.IPictureService;
import com.ncey95.x_image_back.model.service.UserService;
import com.ncey95.x_image_back.model.vo.PictureVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 图片 前端控制器
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-21
 */
@Controller
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private IPictureService pictureService;

    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
}
