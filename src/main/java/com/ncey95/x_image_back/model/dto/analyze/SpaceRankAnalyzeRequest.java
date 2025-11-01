package com.ncey95.x_image_back.model.dto.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest implements Serializable {


    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
