package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1.整合MyBatis-Plus
 *  1)导入依赖
 <dependency>
 <groupId>com.baomidou</groupId>
 <artifactId>mybatis-plus-boot-starter</artifactId>
 <version>3.5.2</version>
 </dependency>
    2）配置
        1.配置数据源
            1）导入数据库的驱动 mysql connector
            2）在application.yml配置数据源相关信息，
        2.配置mybatis-plus
            1)使用@MapperScan
            2)告诉MyBatis-Plus，sql映射文件位置   在application.yml文件中配置


 2.逻辑删除
    1）在yml文件中的mybatis-plus中配置逻辑删除规则（可以省略）
    2）配置逻辑删除组件Bean(可以省略)
    3）在CategoryEntity的逻辑删除字段上加上@TableLogic注解，

 3.JSR303
    1)给entity添加校验注解：javax.validation.constraints包下的注解   并定义自己的错误message提示
    2）开启校验功能，就是在需要校验的entity上添加@Valid注解， 如在CategoryController中的save函数返回参数添加@Valid
        效果：在效验错误后，回提示信息
    3)给效验的实例entity后面紧跟一个BindingResult，就可以通过BindingResult获得效验结果
    4）分组效验(多场景的复杂效验)
        1）	@NotBlank(message = "brand can not be null",groups = {AddGroup.class,UpdateGroup.class})
        给效验注解标注在什么情况下需要校验
        2）@Validated({AddGroup.class})
        3)默认没有指定分组的效验注解@NotNBlank，在分组效验下不会生效，只会在@Validated未分组时生效


 4.统一的异常处理
 @ControllerAdvice
    1)编写异常处理类，使用@ControllerAdvice    GulimallExceptionControllerAdvice
    2）使用@ExceptionHandler标注方法可以处理异常


 6.整合redis
    1）引入data-redis-starter
    2）简单配置redis的host prot等信息
    3）使用SpringBoot自动自动配置，好的StringRedisTemplate；


 7.整合redisson作为分布式锁等功能框架，
    1）引入依赖 redisson
    2) 配置redisson  在MyRedissonConfig中给容器配置一个RedissonClient实例即可，


 8.整合SpringCache简化缓存开发
    1)引入依赖 spring-boot-start-cache   spring-boot-start-data-redis

    2)在application.properties中配置使用redis做为缓存，

    3）开启缓存注解，@EnableCaching

    4)使用注解进行开发，
 *
 * */

//@EnableCaching   //开启缓存功能，写在配置MyCacheConfig配置类中，
@MapperScan("com.atguigu.gulimall.product.dao")   //扫描这个包下面的mapper
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableRedisHttpSession
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
