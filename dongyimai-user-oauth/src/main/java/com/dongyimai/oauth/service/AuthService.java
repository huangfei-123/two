package com.dongyimai.oauth.service;

import com.dongyimai.oauth.util.AuthToken;

public interface AuthService {

    // 密码模式的授权认证
    public AuthToken login(String username,String password,String clientId,String clientSecret);

}
