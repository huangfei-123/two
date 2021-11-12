package com.offcn.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "${mq.queue}")
public class PaySeckillMessageListener {
    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 监听消息
     * @param map 消息队列中的消息
     */
    @RabbitHandler
    public void getMessage(Map<String,String> map){
        System.out.println("监听系统打印信息"+map);
        //通信标识 return_code   SUCCESS/FALL
        //业务结果 result_code   SUCCESS/FALL
        //获取支付交易信息
        String time_end = map.get("time_end");//交易完成时间
        String transaction_id = map.get("transaction_id");//交易流水号
        String out_trade_no = map.get("out_trade_no");//商品订单号，即秒杀订单的id，由idwork生成
        String return_code = map.get("return_code");
        String result_code = map.get("result_code");
        String attach = map.get("attach");//附带的队列信息，队列 用户名
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        String username = attachMap.get("username");

        try {
            if (return_code.equals("SUCCESS")){
                //取得通信
                if (result_code.equals("SUCCESS")){
                    //已支付
                    //修改redis中的秒杀商品订单状态为已支付
                    seckillOrderService.updatePayStatus(time_end,transaction_id,username);
                }else {
                    //支付失败 删除订单回滚库存 [实际开发中存到mysql]
                    seckillOrderService.deleteOrder(username);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
