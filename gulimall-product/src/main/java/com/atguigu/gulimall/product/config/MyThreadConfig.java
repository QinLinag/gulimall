package com.atguigu.gulimall.product.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@EnableConfigurationProperties(ThreadPoolConfigProperties.class)  //开启属性配置，如果ThreadPoolConfigProperties这个类已经注入到容器中了，就不需要开启了
@Configuration
public class MyThreadConfig {


    //自己写的线程池
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool){
       return new ThreadPoolExecutor(pool.getCoreSize(),
               pool.getMaxSize(),
               pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }

}
