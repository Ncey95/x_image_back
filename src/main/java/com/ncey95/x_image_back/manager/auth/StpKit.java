package com.ncey95.x_image_back.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Component
public class StpKit {
    public static final String SPACE_TYPE = "space"; // 空间类型
    public static final StpLogic DEFAULT = StpUtil.stpLogic; // 默认 StpLogic
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE); // 空间 StpLogic
}
