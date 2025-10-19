package com.ncey95.x_image_back.model.controller;

import cn.hutool.core.util.ObjUtil;
import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.model.dto.UserRegisterRequest;
import com.ncey95.x_image_back.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(ObjUtil.isEmpty(userRegisterRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        String userName = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userName, password, checkPassword);
        return ResultUtils.success(result);
    }
}
