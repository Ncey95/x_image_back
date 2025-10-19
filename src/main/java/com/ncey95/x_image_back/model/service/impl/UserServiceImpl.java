package com.ncey95.x_image_back.model.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.model.enums.UserRoleEnum;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.mapper.UserMapper;
import com.ncey95.x_image_back.model.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override // 用户注册
    public long userRegister(String userName, String password, String checkPassword) {
        // 校验参数
        if(StrUtil.hasEmpty(userName, password, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userName.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名长度不能小于6位");
        }
        if(password.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于6位");
        }
        if(!password.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码输入不一致");
        }
        // 2. 校验用户名是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName", userName);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3.加密密码
        String encryptPassword = getEncryptPassword(password);
        // 4. 插入用户
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword(encryptPassword);
        // 5. 插入用户角色
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    public String getEncryptPassword(String password){
        // 密码加密
        final String SALT = "xxxxx";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
}
