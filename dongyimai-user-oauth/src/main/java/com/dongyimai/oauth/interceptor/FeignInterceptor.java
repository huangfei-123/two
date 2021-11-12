package com.dongyimai.oauth.interceptor;

import com.dongyimai.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jwt.Jwt;

@Configuration
public class FeignInterceptor implements RequestInterceptor {
    /**
     * 在Feign执行之前拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Jwt jwt = AdminToken.createJwt();
        //获取最高权限的令牌令牌（设置的admin）
        String token = jwt.getEncoded();
        //将令牌放到头文件中
        requestTemplate.header("Authorization","bearer "+token);
        System.out.println(token);
    }
}