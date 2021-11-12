package com.offcn.entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Feign调用之前进行拦截，王头文件中添加令牌
 */
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            //使用RequestContextHolder工具获取request相关变量,包含请求头和请求参数。
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                //取出request
                HttpServletRequest request = attributes.getRequest();
                //获取所有头文件信息的key
                /**
                 * 注意：如果配置文件中开启了Feign的熔断，默认是线程池隔离，会开启新的线程，
                 * 那么这里获取到的headerNames就是新线程的，就是null。
                 * 需要将熔断的隔开策略换成信号量隔离，这样就不会开启新的线程了。
                 */
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        //头文件的key
                        String name = headerNames.nextElement();
                        //头文件的value
                        String value = request.getHeader(name);
                        //将令牌数据添加到头文件中
                        requestTemplate.header(name, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
