package com.offcn.listener;

import com.offcn.entity.SeckillStatus;
import com.offcn.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RabbitListener(queues = "${mq.seckillQueue}")
public class delaySeckillMessageListener {
    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 监听消息
     * @param seckillStatus 消息队列中的消息
     */
    @RabbitHandler
    public void getMessage(SeckillStatus seckillStatus){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("监听到的时间："+format.format(new Date()));

        //查看是否还有用户的排队信息
        Object userQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());

        if (userQueueStatus!=null){
            //用户未支付 超时了 应关闭微信支付、删除订单
            //关闭微信支付（怎么做？）

            //删除订单（附带回滚库存）和用户排队信息
           seckillOrderService.deleteOrder(seckillStatus.getUsername());
            System.out.println("超时未支付");
        }

    }
}
