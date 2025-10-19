package com.ncey95.x_image_back.model.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ncey95.x_image_back.annotation.AuthCheck;
import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import com.ncey95.x_image_back.constant.UserConstant;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import com.ncey95.x_image_back.model.dto.DeleteRequest;
import com.ncey95.x_image_back.model.dto.user.*;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.po.LoginUserVO;
import com.ncey95.x_image_back.model.po.UserVO;
import com.ncey95.x_image_back.model.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE) 曾用过 测试权限限制注解 authCheck
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
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(ObjUtil.isEmpty(userLoginRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        String userAccount  = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(loginUserVO);
    }
    // 获取当前登录用户
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }
    // 用户注销
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/add") // 添加用户
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();// 创建用户实体
        BeanUtils.copyProperties(userAddRequest, user);// userAddRequest 复制到 user 实体

        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);// 密码加密
        user.setUserPassword(encryptPassword);// 设置加密后的密码
        boolean result = userService.save(user);// 保存用户
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);// 抛出操作错误异常
        return ResultUtils.success(user.getId());// 返回用户ID
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }


    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }


    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/list/page/vo") // 分页查询用户VO脱敏列表
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);// 如果 userQueryRequest 为空 抛出参数错误异常
        long current = userQueryRequest.getCurrent();// 获取当前页码
        long pageSize = userQueryRequest.getPageSize();// 获取每页数量
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));// 分页查询用户实体
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());// 创建用户VO分页实体
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());// 将用户实体列表转换为用户VO列表
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
