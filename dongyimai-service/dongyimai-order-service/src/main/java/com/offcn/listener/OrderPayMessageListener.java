package com.offcn.listener;

import com.offcn.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听mq中的支付消息
 */
@Component
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听消息，修改订单状态
     * @param map 消息队列中传过来的消息
     * @throws Exception
     */
    @RabbitListener(queues = "${mq.queue}")
    public void getMessage(Map<String,String> map) throws Exception {

        System.out.println(map);
        //通信标识 return_code
        String return_code = map.get("return_code");

        if (return_code.equals("SUCCESS")){
            //业务处理结果 result_code，支付成功或支付失败
            String result_code = map.get("result_code");

            //商品订单号 out_trade_no
            String out_trade_no = map.get("out_trade_no");

            if (result_code.equals("SUCCESS")){
                //微信支付流水账号 transaction_id
                String transaction_id = map.get("transaction_id");
                //支付成功，修改订单状态
                orderService.updateStatus(out_trade_no,map.get("time_end"),transaction_id);

            }else {
                //支付失败，关闭支付，同时取消订单，回滚库存
                orderService.deleteOrder(out_trade_no);
                System.out.println("支付失败");

            }

        }


    }
}
