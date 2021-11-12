package com.dongyimai.oauth.service.impl;

import com.dongyimai.oauth.service.AuthService;
import com.dongyimai.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AuthToken login(String username, String password, String clientId,String clientSecret) {
        // 未来可能多有认证中心，获取认证中心的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        if(serviceInstance == null){
            throw new RuntimeException("没有认证中心的服务");
        }
        // 路径的获取
        String path = serviceInstance.getUri().toString() + "/oauth/token";
        // 定义表单的数据
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        // 添加表单数据
        formData.add("grant_type","password");
        formData.add("username",username);
        formData.add("password",password);
        // 请求头的信息
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization",authMessage(clientId,clientSecret));
        // 获取数据
        ResponseEntity<Map> response = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<LinkedMultiValueMap<String, String>>(formData, header), Map.class);
        Map body = response.getBody();
        if(body == null || body.get("access_token") == null || body.get("refresh_token") == null || body.get("jti") == null){
            throw new RuntimeException("返回的令牌有误");
        }
        // 设定返回结果
        AuthToken authToken = new AuthToken();
        authToken.setJti((String)body.get("jti"));
        authToken.setAccessToken((String)body.get("access_token"));
        authToken.setRefreshToken((String) body.get("refresh_token"));
        return authToken;
    }

    private String authMessage(String clientId,String clientSecret){
        String str = clientId + ":" + clientSecret;
        // Base64 加密
        byte[] encode = Base64Utils.encode(str.getBytes());
        // 转换为String 
        return "Basic " + new String(encode);
    }
}
