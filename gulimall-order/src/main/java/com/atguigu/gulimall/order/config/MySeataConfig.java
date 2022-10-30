package com.atguigu.gulimall.order.config;


import io.renren.datasource.annotation.DataSource;
import io.renren.datasource.properties.DataSourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySeataConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;

//    @Bean
//    public DataSource dataSource(){
//        dataSourceProperties
//    }


}
