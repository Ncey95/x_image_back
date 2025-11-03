package com.ncey95.x_image_back.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ncey95.x_image_back.manager.auth.model.SpaceUserAuthConfig;
import com.ncey95.x_image_back.manager.auth.model.SpaceUserRole;
import com.ncey95.x_image_back.model.enums.SpaceRoleEnum;
import com.ncey95.x_image_back.model.enums.SpaceTypeEnum;
import com.ncey95.x_image_back.model.po.Space;
import com.ncey95.x_image_back.model.po.SpaceUser;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.service.SpaceUserService;
import com.ncey95.x_image_back.model.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 空间用户权限配置常量
     * 用于存储从配置文件加载的空间用户权限配置信息
     */
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    /**
     * 静态初始化块
     * 在类加载时从配置文件中读取空间用户权限配置并解析为对象
     */
    static {
        // 读取配置文件内容
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        // 将JSON字符串解析为SpaceUserAuthConfig对象
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据空间用户角色获取对应的权限列表
     *
     * @param spaceUserRole 空间用户角色标识
     * @return 对应角色的权限列表，如果角色不存在则返回空列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        // 校验角色标识是否为空
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }

        // 在配置中查找对应的角色对象
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);

        // 如果角色不存在，返回空列表
        if (role == null) {
            return new ArrayList<>();
        }

        // 返回该角色对应的权限列表
        return role.getPermissions();
    }

    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        switch (spaceTypeEnum) {
            case PRIVATE:
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }
}
