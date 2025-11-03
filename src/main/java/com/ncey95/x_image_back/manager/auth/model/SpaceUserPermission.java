package com.ncey95.x_image_back.manager.auth.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserPermission implements Serializable {
    private String key;
    private String name;
    private String description;
    private static final long serialVersionUID = 1L;
}
