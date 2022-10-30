package com.atguigu.gulimall.product.config;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement  //开启事务
@MapperScan("com.atguigu.gulimall.product.dao")
public class MybatisPlusConfig {

    //  TODO mybatisplus 分页插件



}
