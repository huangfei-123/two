package httpClirent;

import com.offcn.entity.HttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * HttpClient使用案例
 */
public class HttpclientTest {

    //发送Http、Https请求、发送指定参数
    //获取响应结果
    @Test
    public void test() throws IOException {

        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

        //创建HttpClient对象
        HttpClient httpClient = new HttpClient(url);

        //要发送的xml数据
        String xml = "<xml><name>张丹</name></xml>";

        httpClient.setXmlParam(xml);

        //发送请求
        httpClient.setHttps(true);
        httpClient.post();

        //获取响应数据
        String content = httpClient.getContent();
        System.out.println(content);
    }
}
