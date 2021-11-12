package com.offcn.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个controller可以不要，知识用来测试的
 */
@RestController
public class SendController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/")
    public void send() {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", "18716945826");
        map.put("code", "521521");
        //将数据以map方式存到消息队列
        rabbitTemplate.convertAndSend("dongyimai.sms.queue", map);
    }
}
