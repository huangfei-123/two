package com.dongyimai.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 设置最高权限（主要还是去调用/user/{username}/findByUsername方法）
 */
public class AdminToken {
    /**
     * 这是一个登录前所需要的令牌！！！，令牌的内容可以随便给，因为他只是针对findByUsername方法而生！！！
     * 创建令牌oauth微服务访问user微服务查询用户信息时要携带
     */
    public static Jwt createJwt(){
        // 证书文件
        String key_location = "dongyimai.jks";
        // 秘钥库密码
        String key_password = "dongyimai";
        // 秘钥
        String key = "dongyimai";
        // 秘钥别名
        String alias = "dongyimai";

        // 访问证书
        ClassPathResource resource = new ClassPathResource(key_location);
        // 创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, key.toCharArray());
        // 读取秘钥对（公钥和私钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        // 获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();

        //定义 Map
        Map<String, Object> map = new HashMap<>();
        map.put("authorities",new String[]{"admin"});//可以不用给权限，没多大意义！！！
        map.put("name","wo shi deng lu qian de ling pai!");

        // JWT 令牌
        // content 要求是 json格式的字符串
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(privateKey));
        return jwt;
//        // 获取令牌
//        String encoded = jwt.getEncoded();
//        System.out.println(encoded);
    }
}
