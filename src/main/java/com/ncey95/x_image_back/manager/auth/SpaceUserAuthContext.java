package com.ncey95.x_image_back.manager.auth;

import com.ncey95.x_image_back.model.po.Picture;
import com.ncey95.x_image_back.model.po.Space;
import com.ncey95.x_image_back.model.po.SpaceUser;
import lombok.Data;

@Data
public class SpaceUserAuthContext {
    private Long id;
    private Long pictureId;
    private Long spaceId;
    private Long spaceUserId;
    private Picture picture;
    private Space space;
    private SpaceUser spaceUser;
}
