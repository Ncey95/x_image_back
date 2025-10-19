package com.ncey95.x_image_back.model.service;

import com.ncey95.x_image_back.model.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ncey95.x_image_back.model.po.UserLoginVO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
public interface UserService extends IService<User> {

    long userRegister(String userName, String password, String checkPassword); // 用户注册

    UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);
}
