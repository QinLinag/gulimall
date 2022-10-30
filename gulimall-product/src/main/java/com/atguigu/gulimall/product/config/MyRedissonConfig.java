package com.atguigu.gulimall.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是使用RedissonClient对象
     *
     * */

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        //1.创建配置
        Config config = new Config();
        //Redis url should start with redis:// or rediss://
        config.useSingleServer().setAddress("redis://192.168.224.148:6379");

        //2.根据Config创建RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }




















}
