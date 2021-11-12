package com.offcn.listener;

import com.offcn.util.SmsUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/****
 * 简单队列模式
 */
@Component
public class SmsListener {
    @Autowired
    private SmsUtil smsUtil;

    //监听消息队列 yyyyy，里面的消息类型为 map
    //4.消费者监听到消息队列有消息后获取消息
    @RabbitListener(queues = "741")
    public void getMessage(Map<String,String> map) throws Exception {
        if (map == null) {
            return;
        }
        String mobile = map.get("mobile");
        String code = map.get("code");
        // 发送短信
        smsUtil.sendSms(mobile,code);
    }
}
