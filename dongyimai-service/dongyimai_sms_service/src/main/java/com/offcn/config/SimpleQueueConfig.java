package com.offcn.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleQueueConfig {

    //配置消息队列名称
    private String simpleQueue = "741";

    /**
     * 定义简单队列名
     * @return
     */
    @Bean
    public Queue simpleQueue() {
        //创建消息队列
        return new Queue(simpleQueue);
    }
}
