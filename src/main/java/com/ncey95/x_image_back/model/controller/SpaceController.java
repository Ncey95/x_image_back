package com.ncey95.x_image_back.model.controller;

import com.ncey95.x_image_back.annotation.AuthCheck;
import com.ncey95.x_image_back.common.BaseResponse;
import com.ncey95.x_image_back.common.ResultUtils;
import com.ncey95.x_image_back.constant.UserConstant;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import com.ncey95.x_image_back.model.dto.space.SpaceUpdateRequest;
import com.ncey95.x_image_back.model.po.Space;
import com.ncey95.x_image_back.model.service.ISpaceService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 空间 前端控制器
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private ISpaceService spaceService;

    // 更新空间
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        spaceService.fillSpaceBySpaceLevel(space);

        spaceService.validSpace(space, false);

        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
