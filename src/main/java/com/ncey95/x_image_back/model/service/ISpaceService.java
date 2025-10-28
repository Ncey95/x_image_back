package com.ncey95.x_image_back.model.service;

import com.ncey95.x_image_back.model.po.Space;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 空间 服务类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-28
 */
public interface ISpaceService extends IService<Space> {

    void validSpace(Space space, boolean add);

    // 填充空间的默认值
    void fillSpaceBySpaceLevel(Space space);
}
