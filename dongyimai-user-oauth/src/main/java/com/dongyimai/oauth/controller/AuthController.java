package com.dongyimai.oauth.controller;

import com.dongyimai.oauth.service.AuthService;
import com.dongyimai.oauth.util.AuthToken;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

//用户同一到鉴权微服务来登录
@RestController
@RequestMapping("/userAuth")
public class AuthController {

    // 客户端的ID
    @Value("${auth.clientId}")
    private String clientId;//必须与客户端表(oauth_client_details)的字段内容一致,否则就是用户名密码输入正确也登录不了，这是用来客户端信息认证的

    @Value("${auth.clientSecret}")
    private String clientSecret;//必须与客户端表中的字段内容一致，否则就是用户名密码输入正确也登录不了，这是用来客户端信息认证的

    @Autowired
    private AuthService authService;

    //用户同一到鉴权微服务来登录
    @RequestMapping("/login")
    public Result login(String username, String password, HttpServletResponse response){
        // 数据验证
        if(StringUtils.isEmpty(username)){
            throw new RuntimeException("用户名不可以为空");
        }

        if(StringUtils.isEmpty(password)){
            throw new RuntimeException("密码不可以为空");
        }

        // 创建认证令牌对象
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);

        // 获取令牌
        String token = authToken.getAccessToken();

        // 创建Cookie
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        response.addCookie(cookie);
        return new Result(true, StatusCode.OK,"登录成功",token);

    }
}
