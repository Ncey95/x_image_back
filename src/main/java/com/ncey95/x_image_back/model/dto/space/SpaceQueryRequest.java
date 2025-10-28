package com.ncey95.x_image_back.model.dto.space;

import com.ncey95.x_image_back.model.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {


    private Long id;


    private Long userId;


    private String spaceName;


    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}