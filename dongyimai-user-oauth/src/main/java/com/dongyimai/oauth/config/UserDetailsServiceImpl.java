package com.dongyimai.oauth.config;

import com.dongyimai.oauth.util.UserJwt;
import com.offcn.entity.Result;
import com.offcn.feign.UserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.clientId}")
    private String clientId;

    @Autowired
    private UserFeign userFeign; //利用feign来调用user微服务中的查询用户方法（该方法已放行，无需认证）

    /****
     * 自定义授权认证
     * @param username  用户登录的登录名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ********************************客户端数据加载*****************************************
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if (authentication == null) {
            //查询数据库
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //1**写死
                // return new User(clientId,  //客户端id
                //     new BCryptPasswordEncoder().encode(clientSecret), //客户端秘钥 =>加密
                //      AuthorityUtils.commaSeparatedStringToAuthorityList(""));  //权限

                //2**数据库查找方式
                String clientSecret = clientDetails.getClientSecret(); //从数据库中获取客户端秘钥
                  return new User(username,//客户端id
                          clientSecret, //秘钥
                          AuthorityUtils.commaSeparatedStringToAuthorityList(""));//权限
                }
        }
        // ********************************客户端信息认证结束*****************************************





        // ********************************用户数据加载*****************************************
        if (StringUtils.isEmpty(username)) {  //用户输入的数据库中的姓名
            return null;
        }

        //从数据库查询信息
        Result<com.offcn.pojo.User> userResult = userFeign.findByUsername(username);
        if (userResult==null||userResult.getData()==null){
            return null;
        }
        String pwd = userResult.getData().getPassword();//从数据库查询到的用户密码
        //根据用户名查询用户信息
        //String pwd = new BCryptPasswordEncoder().encode("dongyimai");
        //创建User对象
        String permissions = "user,vip,admin"; //指定用户的角色 （本来这里也是应该从数据库查询的，但是tb_user表中没有涉及该字段，这里就随便自定义了几个角色）
        UserJwt userDetails = new UserJwt(username, pwd, AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        return userDetails;
        // ********************************用户名和密码登录开始*****************************************

    }
}
