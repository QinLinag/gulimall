package com.atguigu.gulimall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 *  1.如何使用ali的对象云存储（oss）
 *     1）购买ali的云存储服务器
 *     2）引入spring-cloud-starter-alicloud-oss依赖
 *     3）在application.yml中配置
 *     4）使用OssClient进行相关上传文件到云存储服务器
 * */

@SpringBootApplication
@EnableDiscoveryClient
public class GulimallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallThirdPartyApplication.class, args);
    }

}
