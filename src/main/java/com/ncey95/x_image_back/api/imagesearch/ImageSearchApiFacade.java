package com.ncey95.x_image_back.api.imagesearch;

import com.ncey95.x_image_back.api.imagesearch.model.ImageSearchResult;
import com.ncey95.x_image_back.api.imagesearch.sub.GetImageFirstUrlApi;
import com.ncey95.x_image_back.api.imagesearch.sub.GetImageListApi;
import com.ncey95.x_image_back.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {


    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {

        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
