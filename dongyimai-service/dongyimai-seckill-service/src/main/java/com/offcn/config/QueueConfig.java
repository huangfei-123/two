package com.offcn.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1.延时队列、死信队列，数据的暂时存储queue1
 * 2.受监听的队列queue2
 */
@Configuration
public class QueueConfig {
    //延时队列queue1
    @Bean
    public Queue delaySeckillQueue(){
        return QueueBuilder.durable("delaySeckillQueue")
                .withArgument("x-dead-letter-exchange","seckillExchange")   //当前队列消息一旦过期进入死信交换机
                .withArgument("x-dead-letter-routing-key","seckillQueue")   //将死信队列的消息路由到指定队列
                .build();
    }

    //受监听的队列queue2
    @Bean
    public Queue seckillQueue(){
        return new Queue("seckillQueue");
    }

    //交换机
    @Bean
    public Exchange seckillExchange(){
        return new DirectExchange("seckillExchange");
    }

    //受监听的队列绑定交换机
    @Bean
    public Binding seckillQueueBindingExchange(Queue seckillQueue,Exchange seckillExchange ){
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }
}
