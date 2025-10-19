package com.ncey95.x_image_back.model.dto;

import lombok.Data;

@Data
public class PageRequest {


    private int current = 1;


    private int pageSize = 10;


    private String sortField;


    private String sortOrder = "descend";
}