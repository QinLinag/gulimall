package com.atguigu.gulimall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1.elasticsearch的使用步骤：
 *  1）导入elasticsea rest high level步骤，
 *  2）编写一个config类，
 *
 * */

@EnableRedisHttpSession
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)   //没有用到数据源，那么就要排除common里面的数据源依赖，不然启动服务时回报错
@EnableDiscoveryClient
@EnableFeignClients
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}
