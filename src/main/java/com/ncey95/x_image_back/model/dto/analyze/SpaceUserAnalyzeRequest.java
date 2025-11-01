package com.ncey95.x_image_back.model.dto.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {


    private Long userId;

    // 时间维度：day, week, month, year
    private String timeDimension;
}
