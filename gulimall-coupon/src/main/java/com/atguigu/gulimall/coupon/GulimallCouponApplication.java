package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 *
 * 1.如何使用Nacos作为配置中心统一管理
 *
 * 1）引入spring-cloud-starter-alibaba-nacos-config 依赖
 * 2）创建一个bootstrap.properties
 *    写入：spring.application.name=gulimall-coupon
 *         spring.cloud.nacos.server-addr=127.0.0.1:8848
 * 3)需要在配置中心的配置列表添加一个Data id，默认名字：gulimall-coupon.properties 应用名.properties
 * 4）在gulimall-coupon.properties中写入配置
 * 5）动态获取配置  加上@RefreshScope注解：自动刷新配置    @Value("${配置项的名}")获取到配置
 *                  若配置中心和当前项目的application.properties都添加了配置，那么优先读取配置中心的
 *
 *细节
 * 1）命名空间：用于配置隔离
 *      默认：public（保留空间）；默认新增的所有配置都在public空间
 *      1.开发、测试、生成：利用命名空间来做隔离
 *      注意：要在bootstrap.properties中;配置上需要使用那个命名空间下的配置
 *      spring.cloud.nacos.config.namespace=cd32c595-c8aa-449f-81a7-52c792f6bced
 *      2.每一个微服务之间相互隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 *
 * 2）配置分组
 *      默认所有的配置都是DEFAULT_GROUP
 *
 *
 * 每个微服务创建自己的命名空间，使用配置分组区分环境 dev prop test
 *
 * */

@SpringBootApplication
@EnableDiscoveryClient  //开启服务注册与发现功能，向nacos注册本服务，
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
