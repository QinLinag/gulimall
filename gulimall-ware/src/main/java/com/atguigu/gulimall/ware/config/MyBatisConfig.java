package com.atguigu.gulimall.ware.config;


import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.ware.dao")
public class MyBatisConfig {

    //  TODO mybatisplus 分页插件











}
