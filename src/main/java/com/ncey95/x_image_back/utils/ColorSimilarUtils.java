package com.ncey95.x_image_back.utils;

import java.awt.*;

/**
 * 颜色相似度计算工具类
 * 提供RGB颜色和十六进制颜色字符串的相似度计算功能
 * @author [作者名称]
 */
public class ColorSimilarUtils {

    /**
     * 私有构造函数，防止实例化工具类
     */
    private ColorSimilarUtils() {

    }

    /**
     * 计算两个Color对象的颜色相似度
     * 使用欧几里得距离算法计算RGB空间中的颜色距离，并将其归一化到[0,1]区间
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 颜色相似度，值越大表示颜色越相似（0表示完全不同，1表示完全相同）
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        // 获取第一个颜色的RGB值
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        // 获取第二个颜色的RGB值
        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        // 计算RGB空间中的欧几里得距离
        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));

        // 将距离归一化到[0,1]区间并返回相似度（1-归一化距离）
        // Math.sqrt(3 * Math.pow(255, 2)) 是RGB空间中最大可能的距离（黑色到白色）
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    /**
     * 计算两个十六进制颜色字符串的相似度
     * 该方法是calculateSimilarity(Color, Color)的重载，用于处理字符串类型的颜色表示
     * @param hexColor1 第一个十六进制颜色字符串（格式：#RRGGBB 或 0xRRGGBB）
     * @param hexColor2 第二个十六进制颜色字符串（格式：#RRGGBB 或 0xRRGGBB）
     * @return 颜色相似度，值越大表示颜色越相似（0表示完全不同，1表示完全相同）
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        // 将十六进制颜色字符串转换为Color对象
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        // 调用Color对象版本的相似度计算方法
        return calculateSimilarity(color1, color2);
    }

    /**
     * 测试主方法
     * 用于演示ColorSimilarUtils工具类的使用方法
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 测试Color对象版本的相似度计算
        Color color1 = Color.decode("0xFF0000");  // 红色
        Color color2 = Color.decode("0xFE0101");  // 近似红色
        double similarity = calculateSimilarity(color1, color2);

        System.out.println("颜色相似度为：" + similarity);

        // 测试十六进制字符串版本的相似度计算
        double hexSimilarity = calculateSimilarity("0xFF0000", "0xFE0101");
        System.out.println("十六进制颜色相似度为：" + hexSimilarity);
    }
}
