package weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/****
 * 微信SDK相关测试
 * 1.生成随机字符
 * 2.将MAP转成XML字符串
 * 3.将MAP转成XML字符串,并且带签名
 * 4.将xml字符串转成map
 */
public class weixinUtilTest {

    @Test
    public void testDemo() throws Exception {

        Map<String,String> map = new HashMap<>();
        map.put("id","No.001");
        map.put("title","商城之王");
        map.put("money","998");

        //1.生成随机字符
        String nonceStr = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串："+nonceStr);

        //2.将MAP转成XML字符串
        String xml = WXPayUtil.mapToXml(map);
        System.out.println("xml字符串：\n"+xml);

        //3.将MAP转成XML字符串,并且带签名
        String xmlSigened = WXPayUtil.generateSignedXml(map, "xmlSigened");
        System.out.println("带签名的xml字符串：\n"+xmlSigened);

        //4.将xml转成map
        Map<String, String> toMap = WXPayUtil.xmlToMap(xmlSigened);
        System.out.println("xml转成的map：\n"+toMap);

    }

}
