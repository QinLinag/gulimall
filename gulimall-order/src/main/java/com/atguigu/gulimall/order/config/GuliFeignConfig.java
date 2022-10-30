package com.atguigu.gulimall.order.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {


    //feign远程调用出现请求头丢失问题，
    //首先页面向接口发起请求，接口中通过feign远程调用其他微服务时会创建一个新请求去远程调用其他服务的接口，但是在新的请求中没有页面发来的请求头
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1.RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (null != attributes) {
                    HttpServletRequest request = attributes.getRequest(); //老请求，浏览器发的，
                    if (null != request) {
                        //同步请求头数据，Cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步了老请求cookie
                        requestTemplate.header("Cookie", cookie);
                    }
                }
            }
        };
    }


}
