package com.ncey95.x_image_back.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {


    private Long id;

    private static final long serialVersionUID = 1L;
}
