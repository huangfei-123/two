package com.offcn.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.offcn.entity.HttpClient;
import com.offcn.service.WeiXinPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    //应用id
    @Value("${weixin.appid}")
    private String appid;
    //商户号
    @Value("${weixin.partner}")
    private String partner;
    //秘钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    //支付回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /**
     * 创建二维码（地址）
     * @param paramMap 前段携带的参数
     * @return
     */
    @Override
    public Map createNative(Map<String, String> paramMap) {
        //远程调用参数
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("appid",appid);//应用id
        parameterMap.put("mch_id",partner);//商户id
        parameterMap.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        parameterMap.put("body","东易买");//商品描述
        parameterMap.put("out_trade_no",paramMap.get("out_trade_no"));//商品订单号
        parameterMap.put("total_fee",paramMap.get("totalFee"));//交易总金额，单位：分
        parameterMap.put("spbill_create_ip","192.168.232.130");//客户端ip
        parameterMap.put("notify_url",notifyurl);//交易结果回调通知地址，支付结果通知地址吧
        parameterMap.put("trade_type","NATIVE");//交易类型

        //自定义参数获取，队列类型,因为秒杀订单的支付通知和普通订单的支付通知都要存到MQ中，所以Queue是不同的
        //需要外界指定，不能再配置文件中写死
        //在获取二维码地址时将队列类型以参数方式传过去，待生成的二维码支付后，消息返回数据中有现在携带的参数
        String routingkey = paramMap.get("routingkey");
        String username = paramMap.get("username");
        Map<String,String> map = new HashMap<>();
        map.put("routingkey",routingkey);
        if (!StringUtils.isEmpty(username)){
            //parameterMap.put("username",username);自定义参数要存到attach里面 不然带不过去的
            map.put("username",username);
        }

        String attach = JSON.toJSONString(map);

        parameterMap.put("attach",attach);

        try {
            //将map转成带签名的xml字符串
            String signedXml = WXPayUtil.generateSignedXml(parameterMap, partnerkey);

            //请求的url地址（统一下单api）
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

            //提交方式 https
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);

            //提交参数
            httpClient.setXmlParam(signedXml);

            //执行请求
            httpClient.post();

            //将上面的数据携带到微信客户端后的到的返回数据包括二维码地址  获取返回数据
            String content = httpClient.getContent();

            //将返回数据转成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询支付状态
     * @param out_trade_no 商品订单号
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1.封装参数
            Map<String,String> parameterMap = new HashMap();
            parameterMap.put("appid",appid);                            //应用ID
            parameterMap.put("mch_id",partner);                         //商户id
            parameterMap.put("out_trade_no",out_trade_no);              //商户订单编号
            parameterMap.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符

            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(parameterMap,partnerkey);

            //3、发送请求去查询
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");//API列表 查询订单里面
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
