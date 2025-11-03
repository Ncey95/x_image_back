package com.ncey95.x_image_back.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SpaceUserRole implements Serializable {
    private String key;
    private String name;
    private List<String> permissions;
    private String description;
    private static final long serialVersionUID = 1L;
}
