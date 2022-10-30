package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1.引入amqp场景； RabbitAutoConfiguration就会生效
 * 2.容器中自动配置了   RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3.配置文件中配置信息
 * 4.@EnableRabbit 开启功能
 * 5、监听消息：使用@RabbitListener
 * @Rabbitlistener：在类+方法上面（监听那些队列）
 * @RabbitHandler：标在方法上（重载区分不同的消息）
 *
 *
 *
 *
 * Seata控制分布式事务
 * 1）每一个微服务先必须创建undo_log数据库表
 * 2)安装事务协调器；seata-server:seata服务器下载
 * 3)整合
 *  1.导入依赖 spring-cloud-stater-alibaba-seata   seata-all-0.7.1
 *  2.解压并启动seata-server:
 *      registry.conf：配置中心配置；修改registry type=nacos
 *      file.conf:
 *  3.所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己对的数据源，
 *  4.每个问服务，都必须导入
 *              registry.conf
 *              file.conf   并且修改 vgroup_mapping.{application.name}-fescar-service-group = "default"
 *  5.给分布式大事务的入口标注@GlobalTransactional
 *  6.每一个远程的小事务用@Transaction
 *  7.启动测试分布式事务
 *
 * */


@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRabbit
@SpringBootApplication()
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
