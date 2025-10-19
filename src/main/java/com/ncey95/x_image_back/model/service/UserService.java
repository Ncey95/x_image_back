package com.ncey95.x_image_back.model.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ncey95.x_image_back.model.dto.user.UserQueryRequest;
import com.ncey95.x_image_back.model.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ncey95.x_image_back.model.po.LoginUserVO;
import com.ncey95.x_image_back.model.po.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    boolean userLogout(HttpServletRequest request);// 用户退出登录

    // user转换为UserVO
    UserVO getUserVO(User user);

    // userList转换为UserVOList
    List<UserVO> getUserVOList(List<User> userList);

    // 密码加密 加盐处理  md5 加密
    String getEncryptPassword(String password);

    // 获取查询包装器
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
