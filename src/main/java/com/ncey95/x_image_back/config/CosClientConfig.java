package com.ncey95.x_image_back.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    private String host; // cos 主机地址


    private String secretId; // cos 密钥 id


    private String secretKey; // cos 密钥 key


    private String region; // cos 区域


    private String bucket; // cos 存储桶名称

    @Bean
    public COSClient cosClient() {

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey); // 初始化用户身份信息（secretId, secretKey）

        ClientConfig clientConfig = new ClientConfig(new Region(region)); // 初始化客户端配置（区域）

        return new COSClient(cred, clientConfig);// 初始化COS客户端
    }
}
