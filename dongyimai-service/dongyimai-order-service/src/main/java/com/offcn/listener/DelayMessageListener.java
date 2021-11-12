package com.offcn.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 延时消息监听，然后去数据库查看订单支付状态，如果未支付则库存回滚，删除订单（物理）
 */
@Component
@RabbitListener(queues = "orderListenerQueue")
public class DelayMessageListener {

    @RabbitHandler
    public void getDelayMessage(String message){

        System.out.println("监听到的消息："+message);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("监听到消息的时间："+format.format(new Date()));

        //查看数据库订单状态（作业）
    }
}
