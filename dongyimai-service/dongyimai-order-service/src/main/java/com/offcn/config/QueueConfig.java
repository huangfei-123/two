package com.offcn.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 延时队列配置
 */
@Configuration
public class QueueConfig {
    //创建队列queue1，延时队列会过期，过期后将消息发给queue2
    @Bean
    public Queue orderDelayQueue(){

        return QueueBuilder.durable("orderDelayQueue")
                .withArgument("x-dead-letter-exchange","orderListenerExchange") //死信交换机
                .withArgument("x-dead-letter-routing-key","orderListenerQueue")
                .build();
    }

    //创建队列queue2
    @Bean
    public Queue orderListenerQueue(){
        return new Queue("orderListenerQueue",true);
    }

    //创建交换机
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange("orderListenerExchange");
    }

    //队列queue2绑定交换机
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue,Exchange orderListenerExchange){

        return BindingBuilder
                .bind(orderListenerQueue)
                .to(orderListenerExchange)
                .with("orderListenerQueue")
                .noargs();
    }
}
