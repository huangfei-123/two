package com.offcn.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.service.WeiXinPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/weixin/pay")
public class WeiXinPayController {

    //@Value("${mq.queue}")
    private String simpleQueue;//在mq里面的队列，手动去http://192.168.232.128:15672/#/queues创建一个与之同名的队列

    @Autowired
    private WeiXinPayService weiXinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;//用户将支付消息存储到消息队列

    /**
     * 1.创建支付二维码地址,自定义数据传递
     * 普通订单：routingKey->simpleQueue
     * 秒杀订单：routingKey->seckillQueue
     * 需要的参数如：out_trade_no=d8981fgej548f12167dsf31e5q&totalFee=1&routingkey=queue.seckillorder&username=huangfei
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> paramMap) throws Exception {
        Map<String,String> resultMap = weiXinPayService.createNative(paramMap);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /**
     * 2.微信支付状态查询
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String out_trade_no){
        Map<String,String> resultMap = weiXinPayService.queryPayStatus(out_trade_no);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    /**
     * 3.支付结果回调方法
     * 支付完成后，微信服务器（外网）会把相关支付结果及用户信息通过数据流的形式发送给商户（内网）
     * 内网穿透生成的域名+/weixin/pay/notify/url 就可以实现让微信服务器（外网）调用东易买服务器（内网）的notifyUrl方法了
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){
        System.out.println("进来了！");
        ServletInputStream inputStream;
        try {
            //1.读取支付回调数据
            //专门读取请求数据的一个流
            inputStream = request.getInputStream();
            //2.以前是写到文件中，现在吓到一个字节数组流对象中存储
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            System.out.println(outputStream.toByteArray());//打印数据看看，微信支付结果的字节数组
            outputStream.close();
            inputStream.close();
            //3.字节数组砖字符串，将支付回调数据转换成xml字符串
            String xmlStr = new String(outputStream.toByteArray(), "utf-8");
            //4.将xml字符串（来自微信服务器的支付响应结果）转换成Map结构
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlStr);
            //5.获取自定义参数（前端传入的队列类型，在获取二维码地址时携带到腾讯服务器，在支付通知中携带回来，即此处）
            String attach = resultMap.get("attach");
            Map<String,String> map = JSON.parseObject(attach,Map.class);
            //6.将数据发送给mq (将支付响应结果resultMap转发发送给routingkey队列)
            //在这里第一次用到队列
            rabbitTemplate.convertAndSend(map.get("routingkey"),resultMap); //注意：需要在订单系统设置监听simpleQueue队列的数据
            System.out.println("支付系统打印信息："+resultMap);

            //6.响应数据（给微信服务器）设置
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        } catch (Exception e) {
            e.printStackTrace();
            //记录错误日志
        }
        return null;
    }
}
