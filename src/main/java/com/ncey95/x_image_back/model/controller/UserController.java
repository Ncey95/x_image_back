package com.ncey95.x_image_back.model.controller;

import cn.hutool.core.util.ObjUtil;
import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.model.dto.UserLoginRequest;
import com.ncey95.x_image_back.model.dto.UserRegisterRequest;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.po.UserLoginVO;
import com.ncey95.x_image_back.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
@RestController
@RequestMapping()
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(ObjUtil.isEmpty(userRegisterRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        String userAccount  = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount , password, checkPassword);
        return ResultUtils.success(result);
    }

    // 用户登录
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(ObjUtil.isEmpty(userLoginRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        String userAccount  = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        UserLoginVO userLoginVO = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(userLoginVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUser(loginUser));
    }
}
