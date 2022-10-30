package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {

    //容器中的Binding,Queue,Exchange都会自动创建（Rabbitmq没有的情况下)
    // 如果Rabbitmq已经存在我们想要创建的queue、exchange、binding了后，那么就不会在创建一次，
    // 并且如果我们修改了已有的Binding,Queue,Exchange的属性，那么mq中也不会修改覆盖以前的，也不会创建新的
    @Bean
    public Queue orderDelayQueue(){

        //死信路由，
        Map<String,Object> arguments=new HashMap<>();
        /**
         * x-dead-letter-exchange: order-event-exchange
         * x-dead-letter-routing-key: order.release.order
         * x-message-ttl: 60000
         * */
        arguments.put("x-dead-letter-exchange","order-event-exchange");//这个很重要，如果时间一到，就会通过这个交换机发出去
        arguments.put("x-dead-letter-routing-key","order.release.order");//以这个路由key发送到指定的队列，
        arguments.put("x-message-ttl",60000);
        Queue queue=new Queue("order.delay.queue",true,false,false,arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }





}
