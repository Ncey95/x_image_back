package com.ncey95.x_image_back.model.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ncey95.x_image_back.constant.UserConstant;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.model.enums.UserRoleEnum;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.mapper.UserMapper;
import com.ncey95.x_image_back.model.po.UserLoginVO;
import com.ncey95.x_image_back.model.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override // 用户注册
    public long userRegister(String userAccount, String password, String checkPassword) {
        // 校验参数
        if(StrUtil.hasEmpty(userAccount, password, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length()<5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名长度不能小于5位");
        }
        if(password.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于6位");
        }
        if(!password.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码输入不一致");
        }
        // 2. 校验用户名是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3.加密密码
        String encryptPassword = getEncryptPassword(password);
        // 4. 插入用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        // 4. 插入用户昵称
        user.setUserName(userAccount);
        // 5. 插入用户角色
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    // 用户登录
    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.检验参数
        /*if(StrUtil.hasEmpty(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名长度不能小于5位");
        }
        if(userPassword.length()<7){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于6位");
        }*/

        //2.密码加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 3.1 校验用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserLoginVO(user);
    }

    // 获取当前登录用户
    @Override
    public User getLoginUser(HttpServletRequest request) {

        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    private UserLoginVO getUserLoginVO(User user) {
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userLoginVO);
        return userLoginVO;
    }


    public String getEncryptPassword(String password){
        // 密码加密 加盐处理
        final String SALT = "xxxxx";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
}
