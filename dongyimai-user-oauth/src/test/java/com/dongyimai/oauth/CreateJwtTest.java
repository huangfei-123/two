package com.dongyimai.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTest {

    @Test
    public void createJwt(){
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
        map.put("id",123);
        map.put("username","xiaohei");
        map.put("role","admin");

        // JWT 令牌
        // content 要求是 json格式的字符串
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(privateKey));

        // 获取令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

}
